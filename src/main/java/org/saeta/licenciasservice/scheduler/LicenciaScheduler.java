package org.saeta.licenciasservice.scheduler;

import org.saeta.licenciasservice.service.impl.LicenciaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler para tareas automáticas relacionadas con las licencias
 */
@Component
@Slf4j
public class LicenciaScheduler {

    @Autowired
    private LicenciaServiceImpl licenciaService;

    /**
     * Ejecuta cada hora para desactivar licencias vencidas
     * Cron: segundo(0) minuto(0) hora(*) día(*) mes(*) año(*)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void limpiarLicenciasVencidas() {
        try {
            int licenciasDesactivadas = licenciaService.limpiarLicenciasVencidas();

            if (licenciasDesactivadas > 0) {
                log.info("✅ Licencias vencidas desactivadas: {}", licenciasDesactivadas);
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
            // Aquí puedes agregar lógica para enviar notificaciones
            // sobre licencias que vencen en los próximos 7 días
            log.info("🔔 Verificando licencias próximas a vencer...");

        } catch (Exception e) {
            log.error("❌ Error al verificar licencias próximas a vencer: {}", e.getMessage(), e);
        }
    }
}