package org.saeta.licenciasservice.service;

import org.saeta.licenciasservice.dto.ValidacionRequest;
import org.saeta.licenciasservice.dto.ValidacionResponse;

import java.util.List;

/**
 * Interface del servicio de validación de licencias
 */
public interface LicenciaService {

    /**
     * Valida una licencia por dirección MAC
     * @param request datos de la validación
     * @return respuesta con el resultado de la validación
     */
    ValidacionResponse validarLicencia(ValidacionRequest request);

    /**
     * Valida una licencia usando la MAC del sistema actual
     * @return respuesta con el resultado de la validación
     */
    ValidacionResponse validarLicenciaLocal();

    /**
     * Obtiene todas las direcciones MAC del sistema
     * @return lista de direcciones MAC
     */
    List<String> obtenerDireccionesMac();

    /**
     * Actualiza la fecha de última validación
     * @param mac dirección MAC
     * @return true si se actualizó correctamente
     */
    boolean actualizarUltimaValidacion(String mac);
}