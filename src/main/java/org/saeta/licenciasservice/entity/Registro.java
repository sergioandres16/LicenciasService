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

    // SOLUCIÓN: Usar comillas dobles para preservar el case exacto
    @Column(name = "\"fechaHora\"")
    private LocalDateTime fechaHora;

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