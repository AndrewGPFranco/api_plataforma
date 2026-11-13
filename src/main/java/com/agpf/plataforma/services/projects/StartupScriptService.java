package com.agpf.plataforma.services.projects;

import com.agpf.plataforma.dtos.scripts.InputStartupScriptDTO;
import com.agpf.plataforma.exceptions.ConflictException;
import com.agpf.plataforma.exceptions.NotFoundException;
import com.agpf.plataforma.exceptions.ScriptExecutionException;
import com.agpf.plataforma.repositories.projects.ProjectRepository;
import com.agpf.plataforma.repositories.projects.StartupScriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartupScriptService {

    private final StartupScriptRepository startupScriptRepository;
    private final ProjectRepository projectRepository;
    private final ScriptsEmExecucao scriptsEmExecucao;

    @Transactional
    public String newScript(InputStartupScriptDTO dto) {
        var project = projectRepository.findById(dto.projectId())
                .orElseThrow(() -> new NotFoundException("Projeto não encontrado!"));

        startupScriptRepository.save(InputStartupScriptDTO.toEntity(dto, project));
        return "Script salvo com sucesso";
    }

    @Transactional(readOnly = true)
    public String startProjectLocalhost(UUID id, String path) {
        if (scriptsEmExecucao.rodando(id, path))
            throw new ConflictException("Script já em execução");

        var startupScript = startupScriptRepository.findFirstByProjectIdAndPathProject(id, path)
                .filter(script -> script.getScript() != null)
                .orElseThrow(() -> new NotFoundException("Não foi possível encontrar o comando para a subida do projeto."));

        var processBuilder = new ProcessBuilder("setsid", "sh", "-c", startupScript.getScript()).inheritIO();
        if (startupScript.getPathProject() != null && !startupScript.getPathProject().isBlank())
            processBuilder.directory(new File(startupScript.getPathProject()));

        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            log.error("Erro ao subir a aplicação: {}", e.getMessage());
            throw new ScriptExecutionException("Não foi possível iniciar o ambiente local.", e);
        }

        long pid = process.pid();
        scriptsEmExecucao.marcar(id, path, pid);
        log.info("Projeto {} iniciado (pid {}) com: {}", id, pid, startupScript.getScript());

        process.onExit().thenRun(() -> {
            scriptsEmExecucao.remover(id, path);
            log.info("Projeto {} (pid {}) finalizado.", id, pid);
        });

        return "Ambiente local inicializado.";
    }

    public String encerraProcesso(UUID id, String path) {
        long pid = scriptsEmExecucao.devolvePidProcesso(id, path)
                .orElseThrow(() -> new NotFoundException("Script não está em execução"));

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

        return "Processo encerrado.";
    }

}
