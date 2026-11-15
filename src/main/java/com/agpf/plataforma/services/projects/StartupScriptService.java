package com.agpf.plataforma.services.projects;

import com.agpf.plataforma.dtos.scripts.InputStartupScriptDTO;
import com.agpf.plataforma.exceptions.ConflictException;
import com.agpf.plataforma.exceptions.NotFoundException;
import com.agpf.plataforma.exceptions.ScriptExecutionException;
import com.agpf.plataforma.repositories.projects.ProjectRepository;
import com.agpf.plataforma.repositories.projects.StartupScriptRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
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

        var processBuilder = new ProcessBuilder("setsid", "sh", "-c", startupScript.getScript()).inheritIO();
        if (startupScript.getPathProject() != null && !startupScript.getPathProject().isBlank())
            processBuilder.directory(new File(startupScript.getPathProject()));

        Path arquivo = pastaLogs.resolve(geraNomeLog(id, startupScript.getTitle()));
        processBuilder.redirectOutput(arquivo.toFile());

        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            log.error("Erro ao subir a aplicação: {}", e.getMessage());
            throw new ScriptExecutionException("Não foi possível iniciar o ambiente local.", e);
        }

        long pid = process.pid();
        scriptsEmExecucao.marcar(id, path, pid, title);
        log.info("Projeto {} iniciado (pid {}) com: {}", id, pid, startupScript.getScript());

        process.onExit().thenRun(() -> {
            scriptsEmExecucao.remover(id, path, title);
            log.info("Projeto {} (pid {}) finalizado.", id, pid);
        });

        return "Ambiente local inicializado.";
    }

    public String encerraProcesso(UUID id, String path, String title) {
        long pid = scriptsEmExecucao.devolvePidProcesso(id, path, title)
                .orElseThrow(() -> new NotFoundException("Script não está em execução"));
        String nomeArquivo = geraNomeLog(id, title);
        try {
            int exitCode = new ProcessBuilder("kill", "-TERM", "--", "-" + pid).start().waitFor();
            if (exitCode != 0)
                throw new IOException("kill retornou código " + exitCode);
        } catch (IOException e) {
            log.error("Erro ao encerrar o projeto {} (pid {}): {}", id, pid, e.getMessage());
            throw new ScriptExecutionException("Não foi possível encerrar o processo.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScriptExecutionException("Não foi possível encerrar o processo.", e);
        }

        try {
            Files.deleteIfExists(pastaLogs.resolve(nomeArquivo));
        } catch (IOException io) {
            log.warn("Não foi possível excluir o log {}: {}", nomeArquivo, io.getMessage());
        }

        return "Processo encerrado.";
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

    @PreDestroy
    private void deletaArquivoLogs() {
        File pasta = new File(pastaLogs.toUri());
        for (File arquivoLog : pasta.listFiles()) {
            try {
                Files.delete(pastaLogs.resolve(arquivoLog.getName()));
            } catch (IOException io) {
                log.error("Erro ao deletar o arquivo: {}", arquivoLog.getName());
            }
        }
    }

}
