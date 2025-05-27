package org.saeta.licenciasservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de validación de licencia")
public class ValidacionResponse {

    @Schema(description = "Indica si la licencia es válida", example = "true")
    private boolean valido;

    @Schema(description = "Mensaje descriptivo del resultado", example = "Licencia válida")
    private String mensaje;

    @Schema(description = "Estado de la licencia", example = "ACTIVO")
    private String estado;

    @Schema(description = "Nombre de la empresa", example = "SAETA")
    private String empresa;

    @Schema(description = "Fecha y hora de la validación", example = "2025-05-27T08:45:30")
    private String fechaValidacion;

    @Schema(description = "Última fecha de validación exitosa", example = "2025-05-27T08:40:15")
    private String ultimaValidacion;

    @Schema(description = "Dirección MAC validada", example = "AA-BB-CC-DD-EE-FF")
    private String mac;

    @Schema(description = "Código de error (si aplica)", example = "404")
    private Integer codigoError;
}