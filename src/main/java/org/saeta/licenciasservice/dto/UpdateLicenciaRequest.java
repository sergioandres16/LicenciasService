package org.saeta.licenciasservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateLicenciaRequest {

    private String empresa;

    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
            message = "Formato de MAC inválido. Use XX-XX-XX-XX-XX-XX")
    private String mac;

    @Pattern(regexp = "^[01]$", message = "El estado debe ser '0' o '1'")
    private String estado;

    private String observacion;

    @Positive(message = "La vigencia debe ser un número positivo")
    private Integer vigenciaValor;

    @Pattern(regexp = "^(horas|dias|semanas|meses|anos)$",
            message = "La unidad debe ser: horas, dias, semanas, meses o anos")
    private String vigenciaUnidad;
}