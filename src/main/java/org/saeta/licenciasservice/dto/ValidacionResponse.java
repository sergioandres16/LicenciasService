package org.saeta.licenciasservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de validación de licencia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidacionResponse {

    private boolean valido;

    private String mensaje;

    private String estado;

    private String empresa;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaValidacion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ultimaValidacion;

    private String mac;

    private Integer codigoError;
}
