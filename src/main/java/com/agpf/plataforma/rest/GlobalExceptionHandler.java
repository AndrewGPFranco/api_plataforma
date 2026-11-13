package com.agpf.plataforma.rest;

import com.agpf.plataforma.dtos.http.ResponseMessageDTO;
import com.agpf.plataforma.exceptions.BusinessException;
import com.agpf.plataforma.exceptions.ConflictException;
import com.agpf.plataforma.exceptions.NotFoundException;
import com.agpf.plataforma.exceptions.ScriptExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseMessageDTO handleException(Exception exception) {
        log.error("Erro inesperado", exception);
        return new ResponseMessageDTO("Ocorreu um erro inesperado, tente novamente mais tarde.");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ResponseMessageDTO handleNotFoundException(NotFoundException exception) {
        return new ResponseMessageDTO(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BusinessException.class)
    public ResponseMessageDTO handleBusinessException(BusinessException exception) {
        return new ResponseMessageDTO(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public ResponseMessageDTO handleConflictException(ConflictException exception) {
        return new ResponseMessageDTO(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ScriptExecutionException.class)
    public ResponseMessageDTO handleScriptExecutionException(ScriptExecutionException exception) {
        return new ResponseMessageDTO(exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseMessageDTO handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String mensagem = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage).findFirst().orElse("Dados inválidos.");
        return new ResponseMessageDTO(mensagem);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseMessageDTO handleHttpMessageNotReadableException() {
        return new ResponseMessageDTO("Dados inválidos.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseMessageDTO handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        return new ResponseMessageDTO("Parâmetro inválido: " + exception.getName());
    }

}
