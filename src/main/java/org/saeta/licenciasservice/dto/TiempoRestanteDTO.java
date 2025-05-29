package org.saeta.licenciasservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TiempoRestanteDTO {
    private long dias;
    private long horas;
    private long minutos;
    private long totalMinutos;
}