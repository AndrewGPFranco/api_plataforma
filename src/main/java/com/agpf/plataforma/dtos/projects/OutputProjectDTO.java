package com.agpf.plataforma.dtos.projects;

import com.agpf.plataforma.dtos.scripts.OutputScriptDTO;
import com.agpf.plataforma.models.projects.Project;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OutputProjectDTO(
        UUID id,
        String title,
        String description,
        LocalDate startedAt,
        String repoFrontURL,
        String repoBackURL,
        String deployFrontURL,
        String deployBackURL,
        String localhostURL,
        boolean hasDeploy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Long userId,
        List<OutputScriptDTO> scripts) {

    public static OutputProjectDTO fromEntity(Project entity, List<OutputScriptDTO> scripts) {
        return new OutputProjectDTO(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getStartedAt(),
                entity.getRepoFrontURL(), entity.getRepoBackURL(), entity.getDeployFrontURL(),
                entity.getDeployBackURL(), entity.getLocalhostURL(), entity.isHasDeploy(), entity.getCreatedAt(),
                entity.getUpdatedAt(), entity.getUserId(), scripts);
    }

}
