package com.agpf.plataforma.services.projects;

import com.agpf.plataforma.dtos.projects.CreateProjectDTO;
import com.agpf.plataforma.dtos.projects.EditProjectDTO;
import com.agpf.plataforma.dtos.projects.OutputProjectDTO;
import com.agpf.plataforma.dtos.scripts.OutputScriptDTO;
import com.agpf.plataforma.exceptions.BusinessException;
import com.agpf.plataforma.exceptions.NotFoundException;
import com.agpf.plataforma.models.projects.Project;
import com.agpf.plataforma.models.projects.StartupScript;
import com.agpf.plataforma.repositories.projects.ProjectRepository;
import com.agpf.plataforma.repositories.projects.StartupScriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final StartupScriptRepository startupScriptRepository;
    private final ScriptsEmExecucao scriptsEmExecucao;

    @Transactional
    public OutputProjectDTO createProject(CreateProjectDTO dto) {
        var project = projectRepository.saveAndFlush(CreateProjectDTO.toEntity(dto));
        return OutputProjectDTO.fromEntity(project, List.of());
    }

    @Transactional(readOnly = true)
    public List<OutputProjectDTO> listProjects(Long idUser) {
        List<Project> projects = projectRepository.findByUserIdOrderByCreatedAtDesc(idUser);
        if (projects.isEmpty())
            return List.of();

        Map<UUID, List<StartupScript>> scriptsPorProjeto = startupScriptRepository
                .findByProjectIdIn(projects.stream().map(Project::getId).toList())
                .stream().collect(Collectors.groupingBy(script -> script.getProject().getId()));

        return projects.stream().map(project -> OutputProjectDTO.fromEntity(project,
                scriptsPorProjeto.getOrDefault(project.getId(), List.of()).stream()
                        .map(script -> OutputScriptDTO.fromEntity(script,
                                scriptsEmExecucao.rodando(project.getId(), script.getPathProject())))
                        .toList())).toList();
    }

    @Transactional
    public String deleteProject(UUID id) {
        if (!projectRepository.existsById(id))
            throw new BusinessException("Não foi possivel excluir o projeto.");

        projectRepository.deleteById(id);
        return "Projeto deletado com sucesso!";
    }

    @Transactional
    public String editProject(UUID id, EditProjectDTO dto) {
        var project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Projeto não encontrado!"));

        project.setTitle(dto.title());
        project.setDescription(dto.description());
        project.setStartedAt(dto.startedAt());
        project.setRepoFrontURL(dto.repoFrontURL());
        project.setRepoBackURL(dto.repoBackURL());
        project.setDeployFrontURL(dto.deployFrontURL());
        project.setDeployBackURL(dto.deployBackURL());
        if (dto.localhostURL() != null)
            project.setLocalhostURL(dto.localhostURL());

        projectRepository.save(project);
        return "Metadados atualizados.";
    }

}
