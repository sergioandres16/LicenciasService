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
public class ProyectoDTO {
    private Integer id;
    private String idProducto;
    private String producto;
    private LocalDateTime fechaInicio;
    private String vigencia;
    private Integer vigenciaRestante;
    private String correoVendedor1;
    private String correoVendedor2;
    private String correoJefeVendedor;
    private LocalDateTime fechaCarga;
    private LocalDateTime fechaActualizacion;
    private Boolean activo;
    private Boolean alerta30Enviada;
    private Boolean alerta60Enviada;
    private String estado; // ACTIVO, PROXIMO_A_VENCER, VENCIDO
    private LocalDateTime fechaVencimiento;
}