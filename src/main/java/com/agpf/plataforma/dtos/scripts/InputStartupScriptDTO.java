package com.agpf.plataforma.dtos.scripts;

import com.agpf.plataforma.models.projects.Project;
import com.agpf.plataforma.models.projects.StartupScript;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InputStartupScriptDTO(
        @NotNull(message = "É necessário informar o projeto!") UUID projectId,
        String title,
        String pathProject,
        String script) {

    public static StartupScript toEntity(InputStartupScriptDTO dto, Project project) {
        return StartupScript.builder().project(project).title(dto.title())
                .pathProject(dto.pathProject()).script(dto.script()).build();
    }

}
