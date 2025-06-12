package org.saeta.licenciasservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoCertificadoDTO {
    private Integer id;
    private String nombreCertificado;
    private String abreviatura;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}