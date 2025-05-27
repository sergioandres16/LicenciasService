package org.saeta.licenciasservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que mapea la tabla 'registro' en el esquema 'public'
 */
@Entity
@Table(name = "registro", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "mac")
    private String mac;

    @Column(name = "fechaHora", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime fechaHora;

    // Cambio aquí: especificar que es CHAR(1) en lugar de VARCHAR(1)
    @Column(name = "estado", columnDefinition = "CHAR(1)")
    private String estado;

    /**
     * Verifica si el registro está activo
     * @return true si el estado es '1', false en caso contrario
     */
    public boolean isActivo() {
        return "1".equals(this.estado);
    }
}
