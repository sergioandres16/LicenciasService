package org.saeta.licenciasservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para carga de certificados desde Excel")
public class CertificadoExcelDTO {

    private String fechaEmision;
    private String fechaVencimiento;
    private String ejecutivo;
    private String tipo;
    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String numeroDocumento;
    private String departamento;
    private String cargo;
    private String correoElectronico;
    private String razonSocial;
    private String numeroRuc;
    private String direccion;
    private String codigoPostal;
    private String telefono;
    private String correoEjecutivo1;
    private String correoEjecutivo2;
    private String correoEjecutivo3;
    private String vigencia;

    // Para manejo de errores
    private boolean esValido = true;
    private String error;
}