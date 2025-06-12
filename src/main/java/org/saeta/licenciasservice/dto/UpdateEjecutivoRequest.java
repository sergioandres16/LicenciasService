package org.saeta.licenciasservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateEjecutivoRequest {

    @Size(max = 255, message = "El nombre del ejecutivo no puede exceder 255 caracteres")
    private String nombreEjecutivo;

    @Size(max = 50, message = "La abreviatura no puede exceder 50 caracteres")
    private String abreviatura;

    @Pattern(regexp = "^[01]$", message = "El estado debe ser '0' o '1'")
    private String estado;
}