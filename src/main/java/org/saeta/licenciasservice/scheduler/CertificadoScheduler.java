package org.saeta.licenciasservice.scheduler;

import org.saeta.licenciasservice.service.CertificadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler para tareas automáticas relacionadas con certificados
 */
@Component
@Slf4j
public class CertificadoScheduler {

    @Autowired
    private CertificadoService certificadoService;

    /**
     * Actualiza el estado de vigencia de todos los certificados
     * Se ejecuta todos los días a las 00:30
     */
    @Scheduled(cron = "0 30 0 * * *")
    public void actualizarVigencias() {
        try {
            log.info("🔄 Iniciando actualización de vigencias de certificados...");
            certificadoService.actualizarVigencias();
            log.info("✅ Vigencias de certificados actualizadas exitosamente");
        } catch (Exception e) {
            log.error("❌ Error al actualizar vigencias de certificados: {}", e.getMessage(), e);
        }
    }

    /**
     * Envía alertas de vencimiento para certificados
     * Se ejecuta todos los días a las 9:30 AM
     */
    @Scheduled(cron = "0 30 9 * * *")
    public void enviarAlertasVencimiento() {
        try {
            log.info("📧 Iniciando envío de alertas de vencimiento de certificados...");
            certificadoService.enviarAlertasVencimiento();
            log.info("✅ Alertas de vencimiento de certificados enviadas exitosamente");
        } catch (Exception e) {
            log.error("❌ Error al enviar alertas de vencimiento de certificados: {}", e.getMessage(), e);
        }
    }

    /**
     * Reporte de estado de certificados
     * Se ejecuta el primer día de cada mes a las 8:00 AM
     */
    @Scheduled(cron = "0 0 8 1 * *")
    public void reporteMensualCertificados() {
        try {
            log.info("📊 Generando reporte mensual de estado de certificados...");

            // Aquí podrías agregar lógica para generar y enviar un reporte mensual
            // Por ejemplo: contar certificados vigentes, vencidos, por vencer, etc.

            log.info("✅ Reporte mensual de certificados completado");
        } catch (Exception e) {
            log.error("❌ Error al generar reporte mensual de certificados: {}", e.getMessage(), e);
        }
    }
}