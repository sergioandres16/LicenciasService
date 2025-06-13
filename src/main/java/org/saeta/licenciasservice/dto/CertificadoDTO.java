package org.saeta.licenciasservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de Certificado")
public class CertificadoDTO {

    @Schema(description = "ID del certificado", example = "1")
    private Integer id;

    @Schema(description = "Fecha de emisión", example = "2025-01-01T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaEmision;

    @Schema(description = "Fecha de vencimiento", example = "2026-01-01T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaVencimiento;

    @Schema(description = "ID del ejecutivo", example = "1")
    private Integer ejecutivoId;

    @Schema(description = "Nombre del ejecutivo", example = "Juan Pérez")
    private String ejecutivoNombre;

    @Schema(description = "Tipo de certificado", example = "PERSONA JURIDICA")
    private String tipoCertificado;

    @Schema(description = "Nombres del titular", example = "CARLOS EDUARDO")
    private String nombres;

    @Schema(description = "Primer apellido", example = "LARA")
    private String primerApellido;

    @Schema(description = "Segundo apellido", example = "SEVILLA")
    private String segundoApellido;

    @Schema(description = "Número de documento", example = "73270116")
    private String numeroDocumento;

    @Schema(description = "Departamento", example = "GERENCIA GENERAL")
    private String departamento;

    @Schema(description = "Cargo", example = "GERENTE GENERAL")
    private String cargo;

    @Schema(description = "Correo electrónico", example = "correo@empresa.com")
    private String correoElectronico;

    @Schema(description = "Razón social", example = "EMPRESA S.A.C.")
    private String razonSocial;

    @Schema(description = "Número RUC", example = "20123456789")
    private String numeroRuc;

    @Schema(description = "Dirección", example = "Av. Principal 123")
    private String direccion;

    @Schema(description = "Código postal", example = "15001")
    private String codigoPostal;

    @Schema(description = "Teléfono", example = "987654321")
    private String telefono;

    @Schema(description = "Correo ejecutivo 1", example = "ejecutivo1@empresa.com")
    private String correoEjecutivo1;

    @Schema(description = "Correo ejecutivo 2", example = "ejecutivo2@empresa.com")
    private String correoEjecutivo2;

    @Schema(description = "Correo ejecutivo 3", example = "ejecutivo3@empresa.com")
    private String correoEjecutivo3;

    @Schema(description = "Días de vigencia restantes", example = "365")
    private Integer vigenciaDias;

    @Schema(description = "Estado del certificado", example = "VIGENTE")
    private String estado;

    @Schema(description = "Fecha de carga", example = "2025-01-01T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCarga;

    @Schema(description = "Fecha de actualización", example = "2025-01-01T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaActualizacion;

    @Schema(description = "Indica si está activo", example = "true")
    private Boolean activo;
}