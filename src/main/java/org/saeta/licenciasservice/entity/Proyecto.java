package org.saeta.licenciasservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Entidad que mapea la tabla 'proyectos' en el esquema 'public'
 */
@Entity
@Table(name = "proyectos", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "id_producto", nullable = false, length = 100)
    private String idProducto;

    @Column(name = "producto", nullable = false, length = 255)
    private String producto;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "vigencia", nullable = false, length = 50)
    private String vigencia;

    @Column(name = "vigencia_restante")
    private Integer vigenciaRestante;

    @Column(name = "correo_vendedor1", nullable = false, length = 255)
    private String correoVendedor1;

    @Column(name = "correo_vendedor2", length = 255)
    private String correoVendedor2;

    @Column(name = "correo_jefe_vendedor", length = 255)
    private String correoJefeVendedor;

    @Column(name = "fecha_carga")
    private LocalDateTime fechaCarga = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "alerta_30_enviada")
    private Boolean alerta30Enviada = false;

    @Column(name = "alerta_60_enviada")
    private Boolean alerta60Enviada = false;

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
        // Actualizar vigencia restante antes de cada actualización
        this.actualizarVigenciaRestante();
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
        if (this.alerta30Enviada == null) {
            this.alerta30Enviada = false;
        }
        if (this.alerta60Enviada == null) {
            this.alerta60Enviada = false;
        }
        // Calcular vigencia restante al crear
        this.actualizarVigenciaRestante();
    }

    /**
     * Calcula y actualiza los días de vigencia restante
     */
    public void actualizarVigenciaRestante() {
        if (fechaInicio != null && vigencia != null) {
            LocalDateTime fechaVencimiento = calcularFechaVencimiento();
            if (fechaVencimiento != null) {
                long diasRestantes = ChronoUnit.DAYS.between(LocalDateTime.now(), fechaVencimiento);
                this.vigenciaRestante = Math.max(0, (int) diasRestantes);
            }
        }
    }

    /**
     * Calcula la fecha de vencimiento basándose en fecha de inicio y vigencia
     */
    public LocalDateTime calcularFechaVencimiento() {
        if (fechaInicio == null || vigencia == null) {
            return null;
        }

        // Parsear vigencia similar a como se hace en Licencia
        String vigenciaLower = vigencia.toLowerCase().trim();

        if (vigenciaLower.contains("año") || vigenciaLower.contains("anos")) {
            int anos = extraerNumero(vigencia);
            return fechaInicio.plusYears(anos);
        } else if (vigenciaLower.contains("mes")) {
            int meses = extraerNumero(vigencia);
            return fechaInicio.plusMonths(meses);
        } else if (vigenciaLower.contains("semana")) {
            int semanas = extraerNumero(vigencia);
            return fechaInicio.plusWeeks(semanas);
        } else if (vigenciaLower.contains("día") || vigenciaLower.contains("dia")) {
            int dias = extraerNumero(vigencia);
            return fechaInicio.plusDays(dias);
        }

        return null;
    }

    /**
     * Extrae el número de una cadena de vigencia
     */
    private int extraerNumero(String texto) {
        String numeros = texto.replaceAll("[^0-9]", "");
        try {
            return Integer.parseInt(numeros);
        } catch (NumberFormatException e) {
            return 1; // Por defecto 1 si no se puede parsear
        }
    }

    /**
     * Verifica si el proyecto está próximo a vencer (30 días o menos)
     */
    public boolean isProximoAVencer30() {
        return vigenciaRestante != null && vigenciaRestante <= 30 && vigenciaRestante > 0;
    }

    /**
     * Verifica si el proyecto está próximo a vencer (60 días o menos)
     */
    public boolean isProximoAVencer60() {
        return vigenciaRestante != null && vigenciaRestante <= 60 && vigenciaRestante > 30;
    }

    /**
     * Verifica si el proyecto está vencido
     */
    public boolean isVencido() {
        return vigenciaRestante != null && vigenciaRestante <= 0;
    }

    /**
     * Verifica si necesita alerta de 30 días
     */
    public boolean necesitaAlerta30() {
        return isProximoAVencer30() && !alerta30Enviada && activo;
    }

    /**
     * Verifica si necesita alerta de 60 días
     */
    public boolean necesitaAlerta60() {
        return isProximoAVencer60() && !alerta60Enviada && activo;
    }
}