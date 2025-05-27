package org.saeta.licenciasservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la petición de validación de licencia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionRequest {

    @NotBlank(message = "La dirección MAC es obligatoria")
    private String mac;

    private String empresa;

    private String aplicacion;
}
