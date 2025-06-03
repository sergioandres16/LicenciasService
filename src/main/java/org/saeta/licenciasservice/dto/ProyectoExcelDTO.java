package org.saeta.licenciasservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProyectoExcelDTO {
    private String idProducto;
    private String producto;
    private String fechaInicio; // Como String para facilitar el parseo
    private String vigencia;
    private Integer vigenciaRestante;
    private String correoVendedor1;
    private String correoVendedor2;
    private String correoJefeVendedor;

    // Para manejo de errores
    private String error;
    private boolean valido = true;
}