package org.saeta.licenciasservice.scheduler;

import org.saeta.licenciasservice.entity.Licencia;
import org.saeta.licenciasservice.repository.LicenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Scheduler para tareas automáticas relacionadas con las licencias
 */
@Component
@Slf4j
public class LicenciaScheduler {

    @Autowired
    private LicenciaRepository licenciaRepository;

    @Scheduled(cron = "0 */5 * * * *")
    public void limpiarLicenciasVencidas() {
        try {
            // Obtener todas las licencias activas
            List<Licencia> licenciasActivas = licenciaRepository.findLicenciasActivas();
            int licenciasDesactivadas = 0;

            for (Licencia licencia : licenciasActivas) {
                if (licencia.hasVencido()) {
                    licencia.setEstado("0");
                    licenciaRepository.save(licencia);
                    licenciasDesactivadas++;
                    log.info("Licencia vencida desactivada - ID: {}, MAC: {}, Empresa: {}",
                            licencia.getId(), licencia.getMac(), licencia.getEmpresa());
                }
            }

            if (licenciasDesactivadas > 0) {
                log.info("✅ Total de licencias vencidas desactivadas: {}", licenciasDesactivadas);
            } else {
                log.debug("✅ No hay licencias vencidas para desactivar");
            }

        } catch (Exception e) {
            log.error("❌ Error al limpiar licencias vencidas: {}", e.getMessage(), e);
        }
    }

    /**
     * Ejecuta cada día a las 8:00 AM para reportar licencias próximas a vencer
     * Cron: segundo(0) minuto(0) hora(8) día(*) mes(*) año(*)
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void reportarLicenciasProximasAVencer() {
        try {
            List<Licencia> licenciasActivas = licenciaRepository.findLicenciasActivas();
            int licenciasProximasAVencer = 0;

            log.info("🔔 Verificando licencias próximas a vencer...");

            for (Licencia licencia : licenciasActivas) {
                long diasRestantes = licencia.getDiasRestantes();

                // Reportar licencias que vencen en los próximos 7 días
                if (diasRestantes >= 0 && diasRestantes <= 7) {
                    licenciasProximasAVencer++;

                    var tiempo = licencia.getTiempoRestanteDetallado();
                    String tiempoRestante = "";

                    if (tiempo.getDias() > 0) {
                        tiempoRestante = tiempo.getDias() + " días, " + tiempo.getHoras() + " horas";
                    } else if (tiempo.getHoras() > 0) {
                        tiempoRestante = tiempo.getHoras() + " horas, " + tiempo.getMinutos() + " minutos";
                    } else {
                        tiempoRestante = tiempo.getMinutos() + " minutos";
                    }

                    log.warn("⚠️ Licencia próxima a vencer - ID: {}, MAC: {}, Empresa: {}, Tiempo restante: {}",
                            licencia.getId(), licencia.getMac(), licencia.getEmpresa(), tiempoRestante);
                }
            }

            if (licenciasProximasAVencer > 0) {
                log.info("📊 Total de licencias próximas a vencer (7 días o menos): {}", licenciasProximasAVencer);
            }

        } catch (Exception e) {
            log.error("❌ Error al verificar licencias próximas a vencer: {}", e.getMessage(), e);
        }
    }
}