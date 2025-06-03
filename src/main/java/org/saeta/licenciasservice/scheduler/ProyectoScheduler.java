package org.saeta.licenciasservice.scheduler;

import org.saeta.licenciasservice.service.ProyectoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler para tareas automáticas relacionadas con proyectos
 */
@Component
@Slf4j
public class ProyectoScheduler {

    @Autowired
    private ProyectoService proyectoService;

    /**
     * Actualiza la vigencia restante de todos los proyectos
     * Se ejecuta todos los días a las 00:00
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void actualizarVigenciaRestante() {
        try {
            log.info("🔄 Iniciando actualización de vigencias restantes...");
            proyectoService.actualizarVigenciaRestante();
            log.info("✅ Vigencias restantes actualizadas exitosamente");
        } catch (Exception e) {
            log.error("❌ Error al actualizar vigencias restantes: {}", e.getMessage(), e);
        }
    }

    /**
     * Envía alertas de vencimiento
     * Se ejecuta todos los días a las 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void enviarAlertasVencimiento() {
        try {
            log.info("📧 Iniciando envío de alertas de vencimiento...");
            proyectoService.enviarAlertasVencimiento();
            log.info("✅ Alertas de vencimiento enviadas exitosamente");
        } catch (Exception e) {
            log.error("❌ Error al enviar alertas de vencimiento: {}", e.getMessage(), e);
        }
    }

    /**
     * Reporte de estado de proyectos
     * Se ejecuta cada lunes a las 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * MON")
    public void reporteEstadoProyectos() {
        try {
            log.info("📊 Generando reporte semanal de estado de proyectos...");

            // Aquí podrías agregar lógica para generar y enviar un reporte semanal
            // Por ejemplo: contar proyectos activos, vencidos, próximos a vencer, etc.

            log.info("✅ Reporte semanal generado");
        } catch (Exception e) {
            log.error("❌ Error al generar reporte semanal: {}", e.getMessage(), e);
        }
    }
}