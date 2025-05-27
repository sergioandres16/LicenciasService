package org.saeta.licenciasservice.exception;

import org.saeta.licenciasservice.dto.ValidacionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones
 */
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
                .fechaValidacion(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidacionResponse> handleGeneralException(Exception e) {
        log.error("Error general: {}", e.getMessage(), e);

        ValidacionResponse response = ValidacionResponse.builder()
                .valido(false)
                .mensaje("Error interno del servidor")
                .estado("ERROR_INTERNO")
                .codigoError(500)
                .fechaValidacion(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
