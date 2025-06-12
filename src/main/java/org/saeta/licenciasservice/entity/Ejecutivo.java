package org.saeta.licenciasservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que mapea la tabla 'ejecutivo' en el esquema 'public'
 */
@Entity
@Table(name = "ejecutivo", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ejecutivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nombre_ejecutivo", nullable = false, length = 255)
    private String nombreEjecutivo;

    @Column(name = "abreviatura", nullable = false, length = 50)
    private String abreviatura;

    @Column(name = "estado", columnDefinition = "CHAR(1)")
    private String estado = "1";

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.fechaActualizacion == null) {
            this.fechaActualizacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = "1";
        }
    }

    /**
     * Verifica si el ejecutivo está activo
     * @return true si el estado es '1', false en caso contrario
     */
    public boolean isActivo() {
        return "1".equals(this.estado);
    }
}