package org.saeta.licenciasservice.exception;

import org.saeta.licenciasservice.dto.ValidacionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(LicenciaException.class)
    public ResponseEntity<ValidacionResponse> handleLicenciaException(LicenciaException e) {
        log.error("Error de licencia: {}", e.getMessage());

        ValidacionResponse response = ValidacionResponse.builder()
                .valido(false)
                .mensaje(e.getMessage())
                .estado("ERROR")
                .codigoError(500)
                .fechaValidacion(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("Error de validación: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidacionResponse> handleGenericException(Exception e) {
        log.error("Error inesperado: {}", e.getMessage(), e);

        ValidacionResponse response = ValidacionResponse.builder()
                .valido(false)
                .mensaje("Error interno del servidor")
                .estado("ERROR")
                .codigoError(500)
                .fechaValidacion(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}