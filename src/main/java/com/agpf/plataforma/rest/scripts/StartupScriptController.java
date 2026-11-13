package com.agpf.plataforma.rest.scripts;

import com.agpf.plataforma.dtos.http.ResponseMessageDTO;
import com.agpf.plataforma.dtos.scripts.InputStartupScriptDTO;
import com.agpf.plataforma.services.projects.StartupScriptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scripts")
public class StartupScriptController {

    private final StartupScriptService startupScriptService;

    @PostMapping(value = {"", "/"})
    ResponseEntity<ResponseMessageDTO> newScript(@RequestBody @Valid InputStartupScriptDTO dto) {
        var response = startupScriptService.newScript(dto);
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(new ResponseMessageDTO(response));
    }

    @PostMapping(value = "/start/{id}/{*path}")
    ResponseEntity<ResponseMessageDTO> startProjectLocal(@PathVariable UUID id, @PathVariable String path) {
        var response = startupScriptService.startProjectLocalhost(id, path);
        return ResponseEntity.ok(new ResponseMessageDTO(response));
    }

    @PostMapping(value = "/stop/{id}/{*path}")
    ResponseEntity<ResponseMessageDTO> encerraProjetoLocal(@PathVariable UUID id, @PathVariable String path) {
        var response = startupScriptService.encerraProcesso(id, path);
        return ResponseEntity.ok(new ResponseMessageDTO(response));
    }

}
