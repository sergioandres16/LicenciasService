package org.saeta.licenciasservice.service.impl;

import org.saeta.licenciasservice.dto.ValidacionRequest;
import org.saeta.licenciasservice.dto.ValidacionResponse;
import org.saeta.licenciasservice.entity.Registro;
import org.saeta.licenciasservice.exception.LicenciaException;
import org.saeta.licenciasservice.repository.RegistroRepository;
import org.saeta.licenciasservice.service.LicenciaService;
import org.saeta.licenciasservice.util.MacAddressUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de validación de licencias
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LicenciaServiceImpl implements LicenciaService {

    private final RegistroRepository registroRepository;

    @Override
    @Transactional
    public ValidacionResponse validarLicencia(ValidacionRequest request) {
        log.info("Validando licencia para MAC: {}", request.getMac());

        try {
            // Normalizar dirección MAC
            String macNormalizada = MacAddressUtil.normalizeMacAddress(request.getMac());

            // Validar formato de MAC
            if (!MacAddressUtil.isValidMacAddress(macNormalizada)) {
                return ValidacionResponse.builder()
                        .valido(false)
                        .mensaje("Formato de dirección MAC inválido")
                        .mac(request.getMac())
                        .estado("INVALIDO")
                        .codigoError(400)
                        .fechaValidacion(LocalDateTime.now())
                        .build();
            }

            // Buscar registro en la base de datos
            Optional<Registro> registroOpt = registroRepository.findByMac(macNormalizada);

            if (registroOpt.isEmpty()) {
                log.warn("No se encontró registro para MAC: {}", macNormalizada);
                return ValidacionResponse.builder()
                        .valido(false)
                        .mensaje("Licencia no registrada")
                        .mac(macNormalizada)
                        .estado("NO_REGISTRADO")
                        .codigoError(404)
                        .fechaValidacion(LocalDateTime.now())
                        .build();
            }

            Registro registro = registroOpt.get();

            // Verificar estado de la licencia
            if (!registro.isActivo()) {
                log.warn("Licencia inactiva para MAC: {}", macNormalizada);
                return ValidacionResponse.builder()
                        .valido(false)
                        .mensaje("Licencia inactiva")
                        .mac(macNormalizada)
                        .estado("INACTIVO")
                        .empresa(registro.getEmpresa())
                        .codigoError(403)
                        .fechaValidacion(LocalDateTime.now())
                        .ultimaValidacion(registro.getFechaHora())
                        .build();
            }

            // Actualizar fecha de última validación
            LocalDateTime ahora = LocalDateTime.now();
            registroRepository.actualizarFechaHora(macNormalizada, ahora);

            log.info("Licencia válida para MAC: {} - Empresa: {}", macNormalizada, registro.getEmpresa());

            return ValidacionResponse.builder()
                    .valido(true)
                    .mensaje("Licencia válida")
                    .mac(macNormalizada)
                    .estado("ACTIVO")
                    .empresa(registro.getEmpresa())
                    .fechaValidacion(ahora)
                    .ultimaValidacion(registro.getFechaHora())
                    .build();

        } catch (Exception e) {
            log.error("Error al validar licencia: {}", e.getMessage(), e);
            throw new LicenciaException("Error al validar licencia: " + e.getMessage());
        }
    }

    @Override
    public ValidacionResponse validarLicenciaLocal() {
        log.info("Validando licencia local del sistema");

        // Obtener direcciones MAC del sistema
        List<String> macAddresses = obtenerDireccionesMac();

        if (macAddresses.isEmpty()) {
            return ValidacionResponse.builder()
                    .valido(false)
                    .mensaje("No se pudo obtener la dirección MAC del sistema")
                    .estado("ERROR_SISTEMA")
                    .codigoError(500)
                    .fechaValidacion(LocalDateTime.now())
                    .build();
        }

        // Intentar validar con cada dirección MAC encontrada
        for (String mac : macAddresses) {
            ValidacionRequest request = new ValidacionRequest();
            request.setMac(mac);

            ValidacionResponse response = validarLicencia(request);

            if (response.isValido()) {
                return response;
            }
        }

        // Si ninguna MAC es válida
        return ValidacionResponse.builder()
                .valido(false)
                .mensaje("No se encontró una licencia válida para este equipo")
                .estado("NO_AUTORIZADO")
                .codigoError(401)
                .fechaValidacion(LocalDateTime.now())
                .build();
    }

    @Override
    public List<String> obtenerDireccionesMac() {
        try {
            List<String> macs = MacAddressUtil.getAllMacAddresses();
            log.info("Direcciones MAC encontradas: {}", macs);
            return macs;
        } catch (Exception e) {
            log.error("Error al obtener direcciones MAC: {}", e.getMessage());
            throw new LicenciaException("Error al obtener direcciones MAC del sistema");
        }
    }

    @Override
    @Transactional
    public boolean actualizarUltimaValidacion(String mac) {
        try {
            String macNormalizada = MacAddressUtil.normalizeMacAddress(mac);
            int registrosActualizados = registroRepository.actualizarFechaHora(
                    macNormalizada,
                    LocalDateTime.now()
            );
            return registrosActualizados > 0;
        } catch (Exception e) {
            log.error("Error al actualizar última validación: {}", e.getMessage());
            return false;
        }
    }
}