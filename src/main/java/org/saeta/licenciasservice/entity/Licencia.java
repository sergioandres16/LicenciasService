package org.saeta.licenciasservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que mapea la tabla 'licencia' en el esquema 'public'
 * Incluye los campos originales más observacion y vigencia_dias
 */
@Entity
@Table(name = "licencia", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Licencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "mac")
    private String mac;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "estado", columnDefinition = "CHAR(1)")
    private String estado;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "vigencia_dias")
    private Integer vigenciaDias;

    /**
     * Verifica si el registro está activo
     * @return true si el estado es '1', false en caso contrario
     */
    public boolean isActivo() {
        return "1".equals(this.estado);
    }

    /**
     * Verifica si la licencia ha vencido basándose en fecha_hora + vigencia_dias
     * @return true si la licencia ha vencido, false si aún está vigente
     */
    public boolean hasVencido() {
        if (fechaHora == null || vigenciaDias == null || vigenciaDias <= 0) {
            return true;
        }

        LocalDateTime fechaVencimiento = fechaHora.plusDays(vigenciaDias);
        return LocalDateTime.now().isAfter(fechaVencimiento);
    }

    /**
     * Calcula los días restantes de vigencia
     * @return días restantes (negativo si ya venció)
     */
    public long getDiasRestantes() {
        if (fechaHora == null || vigenciaDias == null) {
            return 0;
        }

        LocalDateTime fechaVencimiento = fechaHora.plusDays(vigenciaDias);
        return java.time.Duration.between(LocalDateTime.now(), fechaVencimiento).toDays();
    }
}