package org.saeta.licenciasservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entidad que mapea la tabla 'certificados' en el esquema 'public'
 */
@Entity
@Table(name = "certificados", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDateTime fechaVencimiento;

    @Column(name = "ejecutivo_id", nullable = false)
    private Integer ejecutivoId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ejecutivo_id", insertable = false, updatable = false)
    private Ejecutivo ejecutivo;

    @Column(name = "tipo_certificado", nullable = false, length = 50)
    private String tipoCertificado;

    @Column(name = "nombres", nullable = false, length = 255)
    private String nombres;

    @Column(name = "primer_apellido", nullable = false, length = 255)
    private String primerApellido;

    @Column(name = "segundo_apellido", length = 255)
    private String segundoApellido;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "cargo", length = 100)
    private String cargo;

    @Column(name = "correo_electronico", length = 255)
    private String correoElectronico;

    @Column(name = "razon_social", length = 255)
    private String razonSocial;

    @Column(name = "numero_ruc", length = 20)
    private String numeroRuc;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "correo_ejecutivo_1", length = 255)
    private String correoEjecutivo1;

    @Column(name = "correo_ejecutivo_2", length = 255)
    private String correoEjecutivo2;

    @Column(name = "correo_ejecutivo_3", length = 255)
    private String correoEjecutivo3;

    @Column(name = "vigencia_dias")
    private Integer vigenciaDias;

    @Column(name = "estado", length = 20)
    private String estado = "VIGENTE";

    @Column(name = "alerta_10_enviada")
    private Boolean alerta10Enviada = false;

    @Column(name = "alerta_20_enviada")
    private Boolean alerta20Enviada = false;

    @Column(name = "alerta_30_enviada")
    private Boolean alerta30Enviada = false;

    @Column(name = "fecha_carga")
    private LocalDateTime fechaCarga = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Column(name = "activo")
    private Boolean activo = true;

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
        this.actualizarVigencia();
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaCarga == null) {
            this.fechaCarga = LocalDateTime.now();
        }
        if (this.fechaActualizacion == null) {
            this.fechaActualizacion = LocalDateTime.now();
        }
        if (this.activo == null) {
            this.activo = true;
        }
        this.actualizarVigencia();
    }

    /**
     * Actualiza el estado y vigencia del certificado
     */
    public void actualizarVigencia() {
        if (this.fechaEmision != null && this.fechaVencimiento != null) {
            LocalDateTime ahora = LocalDateTime.now();

            // Calcular días de vigencia
            long diasRestantes = ChronoUnit.DAYS.between(ahora, this.fechaVencimiento);
            this.vigenciaDias = (int) diasRestantes;

            // Actualizar estado basado en vigencia
            if (diasRestantes < 0) {
                this.estado = "VENCIDO";
            } else if (diasRestantes <= 30) {
                this.estado = "POR_VENCER";
            } else {
                this.estado = "VIGENTE";
            }
        }
    }

    /**
     * Obtiene el nombre completo del titular
     */
    public String getNombreCompleto() {
        StringBuilder nombre = new StringBuilder(this.nombres);
        if (this.primerApellido != null && !this.primerApellido.isEmpty()) {
            nombre.append(" ").append(this.primerApellido);
        }
        if (this.segundoApellido != null && !this.segundoApellido.isEmpty()) {
            nombre.append(" ").append(this.segundoApellido);
        }
        return nombre.toString();
    }

    /**
     * Verifica si el certificado está vencido
     */
    public boolean isVencido() {
        return this.fechaVencimiento != null && this.fechaVencimiento.isBefore(LocalDateTime.now());
    }

    /**
     * Verifica si el certificado está por vencer en los próximos días
     */
    public boolean isPorVencer(int dias) {
        if (this.fechaVencimiento == null) return false;
        LocalDateTime limite = LocalDateTime.now().plusDays(dias);
        return this.fechaVencimiento.isAfter(LocalDateTime.now()) &&
                this.fechaVencimiento.isBefore(limite);
    }
}