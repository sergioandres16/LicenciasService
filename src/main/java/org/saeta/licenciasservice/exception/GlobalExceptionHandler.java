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

        // Extraer solo el primer error para simplificar
        if (!ex.getBindingResult().getFieldErrors().isEmpty()) {
            var firstError = ex.getBindingResult().getFieldErrors().get(0);
            errors.put("message", firstError.getDefaultMessage());
            errors.put("field", firstError.getField());
            errors.put("error", "Validation Error");
        } else {
            errors.put("message", "Error de validación en los datos enviados");
            errors.put("error", "Validation Error");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        log.error("Error de runtime: {}", e.getMessage(), e);

        Map<String, String> error = new HashMap<>();
        error.put("message", e.getMessage());
        error.put("error", "Runtime Error");
        error.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        log.error("Error inesperado: {}", e.getMessage(), e);

        Map<String, String> error = new HashMap<>();
        error.put("message", "Error interno del servidor");
        error.put("error", "Internal Server Error");
        error.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}