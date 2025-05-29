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
public class LicenciaDTO {
    private Integer id;
    private String empresa;
    private String mac;
    private LocalDateTime fechaHora;
    private String estado;
    private String observacion;
    private String vigencia;
    private LocalDateTime fechaVencimiento;
    private Long diasRestantes;
    private Long horasRestantes;
    private Long minutosRestantes;
    private TiempoRestanteDTO tiempoRestante;
    private boolean activo;
    private boolean vencido;
}