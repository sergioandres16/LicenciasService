package org.saeta.licenciasservice.service.impl;

import org.saeta.licenciasservice.dto.ValidacionRequest;
import org.saeta.licenciasservice.dto.ValidacionResponse;
import org.saeta.licenciasservice.entity.Licencia;
import org.saeta.licenciasservice.repository.LicenciaRepository;
import org.saeta.licenciasservice.service.LicenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class LicenciaServiceImpl implements LicenciaService {

    @Autowired
    private LicenciaRepository licenciaRepository;

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
            Optional<Licencia> licenciaOpt = licenciaRepository.findByMac(macNormalizada);

            if (licenciaOpt.isEmpty()) {
                return ValidacionResponse.builder()
                        .valido(false)
                        .mensaje("Licencia no registrada")
                        .mac(macNormalizada)
                        .estado("NO_REGISTRADO")
                        .codigoError(404)
                        .fechaValidacion(LocalDateTime.now().toString())
                        .build();
            }

            Licencia licencia = licenciaOpt.get();

            // Verificar si la licencia ha vencido por tiempo
            if (licencia.hasVencido()) {
                // Actualizar estado a inactivo si venció
                licencia.setEstado("0");
                licenciaRepository.save(licencia);

                return ValidacionResponse.builder()
                        .valido(false)
                        .mensaje("Licencia vencida por tiempo (" + licencia.getVigenciaDias() + " días)")
                        .mac(macNormalizada)
                        .estado("VENCIDO")
                        .empresa(licencia.getEmpresa())
                        .codigoError(403)
                        .fechaValidacion(LocalDateTime.now().toString())
                        .build();
            }

            // Verificar si está activa (estado = '1')
            if (!"1".equals(licencia.getEstado())) {
                return ValidacionResponse.builder()
                        .valido(false)
                        .mensaje("Licencia inactiva")
                        .mac(macNormalizada)
                        .estado("INACTIVO")
                        .empresa(licencia.getEmpresa())
                        .codigoError(403)
                        .fechaValidacion(LocalDateTime.now().toString())
                        .build();
            }

            // Licencia válida
            long diasRestantes = licencia.getDiasRestantes();
            String mensajeVigencia = diasRestantes > 30 ?
                    "Licencia válida (válida por " + diasRestantes + " días más)" :
                    "Licencia válida (¡ATENCIÓN! Vence en " + diasRestantes + " días)";

            return ValidacionResponse.builder()
                    .valido(true)
                    .mensaje(mensajeVigencia)
                    .estado("ACTIVO")
                    .empresa(licencia.getEmpresa())
                    .fechaValidacion(LocalDateTime.now().toString())
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

    /**
     * Método para limpiar licencias vencidas automáticamente
     */
    public int limpiarLicenciasVencidas() {
        return licenciaRepository.desactivarLicenciasVencidas();
    }
}