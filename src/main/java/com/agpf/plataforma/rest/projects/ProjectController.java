package com.agpf.plataforma.rest.projects;

import com.agpf.plataforma.dtos.projects.CreateProjectDTO;
import com.agpf.plataforma.dtos.projects.EditProjectDTO;
import com.agpf.plataforma.dtos.projects.OutputProjectDTO;
import com.agpf.plataforma.services.projects.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping(value = {"", "/"})
    ResponseEntity<OutputProjectDTO> createProject(@RequestBody @Valid CreateProjectDTO dto) {
        var project = projectService.createProject(dto);
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(project);
    }

    @GetMapping(value = "/{idUser}")
    ResponseEntity<List<OutputProjectDTO>> listProjects(@PathVariable Long idUser) {
        return ResponseEntity.ok(projectService.listProjects(idUser));
    }

    @DeleteMapping(value = "/delete/{id}")
    ResponseEntity<String> deleteProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.deleteProject(id));
    }

    @PutMapping(value = "/edit/{id}")
    ResponseEntity<String> editProject(@PathVariable UUID id, @RequestBody @Valid EditProjectDTO dto) {
        return ResponseEntity.ok(projectService.editProject(id, dto));
    }

}
