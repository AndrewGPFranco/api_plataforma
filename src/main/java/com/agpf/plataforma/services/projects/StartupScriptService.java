package com.agpf.plataforma.services.projects;

import com.agpf.plataforma.dtos.scripts.InputStartupScriptDTO;
import com.agpf.plataforma.exceptions.ConflictException;
import com.agpf.plataforma.exceptions.NotFoundException;
import com.agpf.plataforma.exceptions.ScriptExecutionException;
import com.agpf.plataforma.models.projects.CacheScriptRedis;
import com.agpf.plataforma.repositories.projects.ProjectRepository;
import com.agpf.plataforma.repositories.projects.StartupScriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartupScriptService {

    private final ProjectRepository projectRepository;
    private final ScriptsEmExecucao scriptsEmExecucao;
    private final StartupScriptRepository startupScriptRepository;

    private final Path pastaLogs = Paths.get("logs");

    @Transactional
    public String registraNovoScript(InputStartupScriptDTO dto) {
        var project = projectRepository.findById(dto.projectId())
                .orElseThrow(() -> new NotFoundException("Projeto não encontrado!"));

        startupScriptRepository.save(InputStartupScriptDTO.toEntity(dto, project));
        return "Script salvo com sucesso";
    }

    @Transactional(readOnly = true)
    public String iniciaProjetoLocal(UUID id, String path, String title) {
        if (scriptsEmExecucao.rodando(id, path, title))
            throw new ConflictException("Script já em execução");

        var startupScript = startupScriptRepository.findFirstByProjectIdAndPathProject(id, path)
                .filter(script -> script.getScript() != null)
                .orElseThrow(() -> new NotFoundException("Não foi possível encontrar o comando para a subida do projeto."));

        // setsid: o script roda em sessão/grupo próprio e sobrevive à parada da plataforma.
        // stdin/stdout/stderr totalmente desligados da JVM, para o script não morrer por SIGPIPE quando ela cair.
        var processBuilder = new ProcessBuilder("setsid", "sh", "-c", startupScript.getScript())
                .redirectInput(ProcessBuilder.Redirect.from(new File("/dev/null")))
                .redirectErrorStream(true);
        if (startupScript.getPathProject() != null && !startupScript.getPathProject().isBlank())
            processBuilder.directory(new File(startupScript.getPathProject()));

        Process process;
        try {
            Files.createDirectories(pastaLogs);
            processBuilder.redirectOutput(pastaLogs.resolve(geraNomeLog(id, startupScript.getTitle())).toFile());
            process = processBuilder.start();
        } catch (IOException e) {
            log.error("Erro ao subir a aplicação: {}", e.getMessage());
            throw new ScriptExecutionException("Não foi possível iniciar o ambiente local.", e);
        }

        ProcessHandle handle = process.toHandle();
        CacheScriptRedis registro;
        try {
            registro = scriptsEmExecucao.marcar(id, path, handle, title);
        } catch (RuntimeException e) {
            log.error("Erro ao registrar o projeto {} (pid {}), encerrando o processo: {}", id, handle.pid(), e.getMessage());
            matarGrupo(handle.pid());
            throw e;
        }

        monitora(registro, handle);
        log.info("Projeto {} iniciado (pid {}) com: {}", id, handle.pid(), startupScript.getScript());

        return "Ambiente local inicializado.";
    }

    public String encerraProcesso(UUID id, String path, String title) {
        CacheScriptRedis registro = scriptsEmExecucao.buscar(id, title)
                .orElseThrow(() -> new NotFoundException("Script não está em execução"));

        if (scriptsEmExecucao.processoVivo(registro).isEmpty()) {
            scriptsEmExecucao.remover(registro);
            throw new NotFoundException("Script não está em execução");
        }

        long pid = registro.getPid();
        try {
            matarGrupo(pid);
        } catch (ScriptExecutionException e) {
            log.error("Erro ao encerrar o projeto {} (pid {}): {}", id, pid, e.getMessage());
            throw e;
        }

        scriptsEmExecucao.remover(registro);
        deletaLog(geraNomeLog(id, title));

        return "Processo encerrado.";
    }

    /**
     * Ao subir, reassume os scripts que ficaram rodando enquanto a plataforma estava fora:
     * remove registros de processos que morreram e volta a monitorar os que continuam vivos.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void reconciliaScripts() {
        for (CacheScriptRedis registro : scriptsEmExecucao.todos()) {
            if (registro == null) continue;

            scriptsEmExecucao.processoVivo(registro).ifPresentOrElse(
                    handle -> {
                        monitora(registro, handle);
                        log.info("Projeto {} (pid {}) ainda em execução, monitoramento retomado.",
                                registro.getProjectId(), registro.getPid());
                    },
                    () -> {
                        scriptsEmExecucao.remover(registro);
                        log.info("Projeto {} (pid {}) finalizou enquanto a plataforma estava fora, registro removido.",
                                registro.getProjectId(), registro.getPid());
                    });
        }
    }

    private void monitora(CacheScriptRedis registro, ProcessHandle handle) {
        handle.onExit().thenRun(() -> {
            scriptsEmExecucao.remover(registro);
            log.info("Projeto {} (pid {}) finalizado.", registro.getProjectId(), registro.getPid());
        });
    }

    private void matarGrupo(long pid) {
        try {
            int exitCode = new ProcessBuilder("kill", "-TERM", "--", "-" + pid).start().waitFor();
            if (exitCode != 0)
                throw new IOException("kill retornou código " + exitCode);
        } catch (IOException e) {
            throw new ScriptExecutionException("Não foi possível encerrar o processo.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScriptExecutionException("Não foi possível encerrar o processo.", e);
        }
    }

    private void deletaLog(String nomeArquivo) {
        try {
            Files.deleteIfExists(pastaLogs.resolve(nomeArquivo));
        } catch (IOException io) {
            log.warn("Não foi possível excluir o log {}: {}", nomeArquivo, io.getMessage());
        }
    }

    private String geraNomeLog(UUID id, String title) {
        return id.toString().concat(title).concat(".log");
    }

    public String obtemLogs(UUID id, String title) {
        Path arquivo = pastaLogs.resolve(geraNomeLog(id, title));
        if (!Files.exists(arquivo)) return null;

        try {
            return Files.readString(arquivo);
        } catch (IOException io) {
            throw new RuntimeException("Ocorreu um erro ao montar a resposta do log", io);
        }
    }

}
