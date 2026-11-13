package com.agpf.plataforma.services.projects;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ScriptsEmExecucao {

    private record ScriptKey(UUID projectId, String path) {
    }

    private final Map<ScriptKey, Long> scripts = new ConcurrentHashMap<>();

    public boolean rodando(UUID projectId, String path) {
        if (path == null)
            return false;
        return scripts.containsKey(new ScriptKey(projectId, path));
    }

    public boolean marcar(UUID projectId, String path, long pid) {
        return scripts.putIfAbsent(new ScriptKey(projectId, path), pid) == null;
    }

    public void remover(UUID projectId, String path) {
        scripts.remove(new ScriptKey(projectId, path));
    }

    public Optional<Long> devolvePidProcesso(UUID projectId, String path) {
        return Optional.ofNullable(scripts.get(new ScriptKey(projectId, path)));
    }

}
