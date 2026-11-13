package com.agpf.plataforma.dtos.projects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

public record EditProjectDTO(
        @NotBlank(message = "É necessário informar o título do projeto!") String title,
        String description,
        @NotNull(message = "É necessário informar a data de início do projeto!") LocalDate startedAt,
        @URL(message = "A URL do repositório front-end é inválida.") String repoFrontURL,
        @URL(message = "A URL do repositório back-end é inválida.") String repoBackURL,
        @URL(message = "A URL do deploy front-end é inválida.") String deployFrontURL,
        @URL(message = "A URL do deploy back-end é inválida.") String deployBackURL,
        @URL(message = "A URL localhost é inválida.") String localhostURL) {

}
