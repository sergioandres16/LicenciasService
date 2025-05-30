package org.saeta.licenciasservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entidad que mapea la tabla 'licencia' en el esquema 'public'
 * Usa campo vigencia como texto: "1 hora", "3 días", etc.
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

    @Column(name = "vigencia", length = 50)
    private String vigencia;

    /**
     * Verifica si el registro está activo
     * @return true si el estado es '1', false en caso contrario
     */
    public boolean isActivo() {
        return "1".equals(this.estado);
    }

    /**
     * Convierte la vigencia texto a minutos
     */
    public Integer getVigenciaEnMinutos() {
        if (vigencia == null || vigencia.trim().isEmpty()) {
            return null;
        }

        // Patrón mejorado para capturar todas las variaciones posibles
        Pattern pattern = Pattern.compile("(\\d+)\\s*(hora|horas|día|dias|día|días|semana|semanas|mes|meses|año|anos|año|años)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(vigencia.trim());

        if (matcher.find()) {
            int valor = Integer.parseInt(matcher.group(1));
            String unidad = matcher.group(2).toLowerCase();

            // Log para debugging
            System.out.println("Vigencia: " + vigencia + " -> Valor: " + valor + ", Unidad: " + unidad);

            // Normalizar la unidad removiendo la 's' del plural
            if (unidad.endsWith("s") && !unidad.equals("mes")) {
                unidad = unidad.substring(0, unidad.length() - 1);
            }

            // Normalizar acentos
            unidad = unidad.replace("í", "i").replace("ñ", "n");

            // Manejar el caso especial de "mes" y "meses"
            if (unidad.equals("mese")) {
                unidad = "mes";
            }

            switch (unidad) {
                case "hora":
                    return valor * 60;
                case "dia":
                    return valor * 24 * 60;
                case "semana":
                    return valor * 7 * 24 * 60;
                case "mes":
                case "meses":  // Agregar explícitamente "meses"
                    return valor * 30 * 24 * 60;
                case "ano":
                    return valor * 365 * 24 * 60;
                default:
                    System.err.println("Unidad no reconocida: " + unidad);
                    return null;
            }
        }

        return null;
    }

    /**
     * Verifica si la licencia ha vencido
     */
    public boolean hasVencido() {
        if (!isActivo()) {
            return false; // Si está inactivo, no se considera vencido
        }

        if (fechaHora == null || vigencia == null) {
            return true;
        }

        Integer minutos = getVigenciaEnMinutos();
        if (minutos == null || minutos <= 0) {
            return true;
        }

        LocalDateTime fechaVencimiento = fechaHora.plusMinutes(minutos);
        return LocalDateTime.now().isAfter(fechaVencimiento);
    }

    /**
     * Calcula el tiempo restante
     */
    public TiempoRestante getTiempoRestanteDetallado() {
        if (!isActivo() || fechaHora == null || vigencia == null) {
            return new TiempoRestante(0, 0, 0, 0);
        }

        Integer minutos = getVigenciaEnMinutos();
        if (minutos == null) {
            return new TiempoRestante(0, 0, 0, 0);
        }

        LocalDateTime fechaVencimiento = fechaHora.plusMinutes(minutos);
        LocalDateTime ahora = LocalDateTime.now();

        // Si ya venció
        if (ahora.isAfter(fechaVencimiento)) {
            Duration duration = Duration.between(fechaVencimiento, ahora);
            long totalMinutos = -duration.toMinutes(); // Negativo porque ya pasó
            return new TiempoRestante(0, 0, 0, totalMinutos);
        }

        // Si aún tiene vigencia
        Duration duration = Duration.between(ahora, fechaVencimiento);
        long totalMinutos = duration.toMinutes();

        if (totalMinutos <= 0) {
            return new TiempoRestante(0, 0, 0, 0);
        }

        long dias = totalMinutos / (24 * 60);
        long horasRestantes = (totalMinutos % (24 * 60)) / 60;
        long minutosRestantes = totalMinutos % 60;

        return new TiempoRestante(dias, horasRestantes, minutosRestantes, totalMinutos);
    }

    /**
     * Obtiene los días restantes
     */
    public long getDiasRestantes() {
        TiempoRestante tiempo = getTiempoRestanteDetallado();
        return tiempo.getDias();
    }

    /**
     * Obtiene las horas restantes totales
     */
    public long getHorasRestantes() {
        TiempoRestante tiempo = getTiempoRestanteDetallado();
        return Math.max(0, tiempo.getTotalMinutos() / 60);
    }

    /**
     * Obtiene los minutos restantes totales
     */
    public long getMinutosRestantes() {
        TiempoRestante tiempo = getTiempoRestanteDetallado();
        return tiempo.getTotalMinutos();
    }

    @Data
    @AllArgsConstructor
    public static class TiempoRestante {
        private long dias;
        private long horas;
        private long minutos;
        private long totalMinutos;
    }
}