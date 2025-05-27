package org.saeta.licenciasservice.service;

import org.saeta.licenciasservice.dto.ValidacionRequest;
import org.saeta.licenciasservice.dto.ValidacionResponse;

public interface LicenciaService {

    /**
     * Valida una licencia por MAC address
     * @param request Datos de la validación incluyendo MAC
     * @return ValidacionResponse con el resultado
     */
    ValidacionResponse validarLicencia(ValidacionRequest request);

    /**
     * Valida si una MAC es válida en formato
     * @param mac Dirección MAC a validar
     * @return true si es válida, false si no
     */
    boolean esMacValida(String mac);
}