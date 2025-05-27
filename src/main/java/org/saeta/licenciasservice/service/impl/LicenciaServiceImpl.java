package org.saeta.licenciasservice.service.impl;

import org.saeta.licenciasservice.dto.ValidacionRequest;
import org.saeta.licenciasservice.dto.ValidacionResponse;
import org.saeta.licenciasservice.entity.Registro;
import org.saeta.licenciasservice.repository.RegistroRepository;
import org.saeta.licenciasservice.service.LicenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class LicenciaServiceImpl implements LicenciaService {

    @Autowired
    private RegistroRepository registroRepository;

    // Patrón para validar formato MAC (acepta : y -)
    private static final Pattern MAC_PATTERN = Pattern.compile(
            "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
    );

    @Override
    public ValidacionResponse validarLicencia(ValidacionRequest request) {
        try {
            String mac = request.getMac();

            // Validar formato de MAC
            if (!esMacValida(mac)) {
                return ValidacionResponse.builder()
                        .valido(false)
                        .mensaje("Formato de dirección MAC inválido")
                        .mac(mac)
                        .estado("INVALIDO")
                        .codigoError(400)
                        .fechaValidacion(LocalDateTime.now().toString())
                        .build();
            }

            // Normalizar MAC (convertir : a -)
            String macNormalizada = normalizarMac(mac);

            // Buscar en base de datos
            Optional<Registro> registroOpt = registroRepository.findByMac(macNormalizada);

            if (registroOpt.isEmpty()) {
                return ValidacionResponse.builder()
                        .valido(false)
                        .mensaje("Licencia no registrada")
                        .mac(macNormalizada)
                        .estado("NO_REGISTRADO")
                        .codigoError(404)
                        .fechaValidacion(LocalDateTime.now().toString())
                        .build();
            }

            Registro registro = registroOpt.get();

            // Verificar si está activa (estado = '1')
            if (!"1".equals(registro.getEstado())) {
                return ValidacionResponse.builder()
                        .valido(false)
                        .mensaje("Licencia inactiva")
                        .mac(macNormalizada)
                        .estado("INACTIVO")
                        .empresa(registro.getEmpresa())
                        .codigoError(403)
                        .fechaValidacion(LocalDateTime.now().toString())
                        .ultimaValidacion(registro.getFechaHora().toString())
                        .build();
            }

            // Actualizar fecha de último acceso
            registro.setFechaHora(LocalDateTime.now());
            registroRepository.save(registro);

            // Licencia válida
            return ValidacionResponse.builder()
                    .valido(true)
                    .mensaje("Licencia válida")
                    .estado("ACTIVO")
                    .empresa(registro.getEmpresa())
                    .fechaValidacion(LocalDateTime.now().toString())
                    .ultimaValidacion(registro.getFechaHora().toString())
                    .mac(macNormalizada)
                    .build();

        } catch (Exception e) {
            return ValidacionResponse.builder()
                    .valido(false)
                    .mensaje("Error interno del servidor: " + e.getMessage())
                    .estado("ERROR")
                    .codigoError(500)
                    .fechaValidacion(LocalDateTime.now().toString())
                    .build();
        }
    }

    @Override
    public boolean esMacValida(String mac) {
        if (mac == null || mac.trim().isEmpty()) {
            return false;
        }
        return MAC_PATTERN.matcher(mac.trim()).matches();
    }

    /**
     * Normaliza la MAC convirtiendo : a - y poniendo en mayúsculas
     */
    private String normalizarMac(String mac) {
        return mac.trim().replace(":", "-").toUpperCase();
    }
}