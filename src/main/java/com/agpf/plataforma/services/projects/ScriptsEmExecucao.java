package com.agpf.plataforma.services.projects;

import com.agpf.plataforma.models.projects.CacheScriptRedis;
import com.agpf.plataforma.repositories.projects.CacheScriptRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ScriptsEmExecucao {

    private final CacheScriptRedisRepository cacheRepo;

    public boolean rodando(UUID projectId, String path, String title) {
        if (path == null)
            return false;

        return cacheRepo.findByProjectIdAndTitle(projectId, title).isPresent();
    }

    public CacheScriptRedis marcar(UUID projectId, String path, ProcessHandle processo, String title) {
        CacheScriptRedis cacheScript = new CacheScriptRedis(UUID.randomUUID(), processo.pid(), path, title, projectId,
                processo.info().startInstant().orElse(null));
        return cacheRepo.save(cacheScript);
    }

    public void remover(CacheScriptRedis script) {
        cacheRepo.deleteById(script.getId());
    }

    public Optional<CacheScriptRedis> buscar(UUID projectId, String title) {
        return cacheRepo.findByProjectIdAndTitle(projectId, title);
    }

    public Iterable<CacheScriptRedis> todos() {
        return cacheRepo.findAll();
    }

    /**
     * Devolve o processo do script se ele ainda estiver vivo E for o mesmo processo registrado
     * (mesmo horário de início), evitando agir sobre um PID que o SO reutilizou.
     */
    public Optional<ProcessHandle> processoVivo(CacheScriptRedis script) {
        return ProcessHandle.of(script.getPid())
                .filter(ProcessHandle::isAlive)
                .filter(p -> Objects.equals(p.info().startInstant().orElse(null), script.getInicio()));
    }

}
