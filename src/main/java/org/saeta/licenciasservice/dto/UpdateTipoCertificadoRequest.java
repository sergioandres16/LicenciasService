package org.saeta.licenciasservice.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTipoCertificadoRequest {

    @Size(max = 255, message = "El nombre del certificado no puede exceder 255 caracteres")
    private String nombreCertificado;

    @Size(max = 50, message = "La abreviatura no puede exceder 50 caracteres")
    private String abreviatura;
}