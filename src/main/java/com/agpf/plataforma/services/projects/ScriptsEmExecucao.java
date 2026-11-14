package com.agpf.plataforma.services.projects;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ScriptsEmExecucao {

    private record ScriptKey(UUID projectId, String path, String title) {
    }

    private final Map<ScriptKey, Long> scripts = new ConcurrentHashMap<>();

    public boolean rodando(UUID projectId, String path, String title) {
        if (path == null)
            return false;
        return scripts.containsKey(new ScriptKey(projectId, path, title));
    }

    public boolean marcar(UUID projectId, String path, long pid, String title) {
        return scripts.putIfAbsent(new ScriptKey(projectId, path, title), pid) == null;
    }

    public void remover(UUID projectId, String path, String title) {
        scripts.remove(new ScriptKey(projectId, path, title));
    }

    public Optional<Long> devolvePidProcesso(UUID projectId, String path, String title) {
        return Optional.ofNullable(scripts.get(new ScriptKey(projectId, path, title)));
    }

}
