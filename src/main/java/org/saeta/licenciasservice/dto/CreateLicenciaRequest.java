package org.saeta.licenciasservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateLicenciaRequest {

    @NotBlank(message = "La empresa es obligatoria")
    private String empresa;

    @NotBlank(message = "La dirección MAC es obligatoria")
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
            message = "Formato de MAC inválido. Use XX-XX-XX-XX-XX-XX")
    private String mac;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^[01]$", message = "El estado debe ser '0' o '1'")
    private String estado;

    private String observacion;

    @NotNull(message = "La vigencia es obligatoria")
    @Positive(message = "La vigencia debe ser un número positivo")
    private Integer vigenciaValor;

    @NotBlank(message = "La unidad de vigencia es obligatoria")
    @Pattern(regexp = "^(horas|dias|semanas|meses|anos)$",
            message = "La unidad debe ser: horas, dias, semanas, meses o anos")
    private String vigenciaUnidad;
}