package com.agpf.plataforma.dtos.scripts;

import com.agpf.plataforma.models.projects.StartupScript;

public record OutputScriptDTO(
        String command,
        String path,
        String title,
        boolean running) {

    public static OutputScriptDTO fromEntity(StartupScript entity, boolean running) {
        return new OutputScriptDTO(entity.getScript(), entity.getPathProject(), entity.getTitle(), running);
    }

}
