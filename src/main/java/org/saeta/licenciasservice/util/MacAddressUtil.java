package org.saeta.licenciasservice.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Utilidad para obtener direcciones MAC del sistema
 */
@Slf4j
public class MacAddressUtil {

    /**
     * Obtiene la dirección MAC del sistema
     * @param ip dirección IP (opcional)
     * @return dirección MAC en formato XX-XX-XX-XX-XX-XX
     */
    public static String getMacAddress(String ip) {
        try {
            InetAddress address = (ip != null && !ip.isEmpty())
                    ? InetAddress.getByName(ip)
                    : InetAddress.getLocalHost();

            NetworkInterface ni = NetworkInterface.getByInetAddress(address);

            if (ni != null) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    return formatMacAddress(mac);
                }
            }

            // Si no se encuentra, buscar en todas las interfaces
            return getFirstAvailableMacAddress();

        } catch (UnknownHostException | SocketException e) {
            log.error("Error al obtener dirección MAC: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene todas las direcciones MAC del sistema
     * @return lista de direcciones MAC
     */
    public static List<String> getAllMacAddresses() {
        List<String> macAddresses = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();

            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();
                byte[] mac = network.getHardwareAddress();

                if (mac != null && mac.length > 0 && !isVirtualMac(mac)) {
                    String macAddress = formatMacAddress(mac);
                    if (!macAddresses.contains(macAddress)) {
                        macAddresses.add(macAddress);
                    }
                }
            }
        } catch (SocketException e) {
            log.error("Error al obtener direcciones MAC: {}", e.getMessage());
        }

        return macAddresses;
    }

    /**
     * Obtiene la primera dirección MAC disponible (no virtual)
     * @return dirección MAC o null si no se encuentra
     */
    private static String getFirstAvailableMacAddress() {
        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();

            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();

                // Saltar interfaces virtuales o loopback
                if (network.isLoopback() || network.isVirtual() || !network.isUp()) {
                    continue;
                }

                byte[] mac = network.getHardwareAddress();

                if (mac != null && mac.length > 0 && !isVirtualMac(mac)) {
                    return formatMacAddress(mac);
                }
            }
        } catch (SocketException e) {
            log.error("Error al buscar direcciones MAC: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Formatea un array de bytes a formato de dirección MAC
     * @param mac array de bytes
     * @return dirección MAC formateada
     */
    private static String formatMacAddress(byte[] mac) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
        }
        return sb.toString();
    }

    /**
     * Verifica si una dirección MAC pertenece a una máquina virtual
     * @param mac array de bytes de la dirección MAC
     * @return true si es una MAC virtual
     */
    private static boolean isVirtualMac(byte[] mac) {
        if (mac == null || mac.length < 3) return false;

        byte[][] virtualMacs = {
                {0x00, 0x05, 0x69},         // VMWare
                {0x00, 0x1C, 0x14},         // VMWare
                {0x00, 0x0C, 0x29},         // VMWare
                {0x00, 0x50, 0x56},         // VMWare
                {0x08, 0x00, 0x27},         // VirtualBox
                {0x0A, 0x00, 0x27},         // VirtualBox
                {0x00, 0x03, (byte)0xFF},   // Virtual-PC
                {0x00, 0x15, 0x5D}          // Hyper-V
        };

        for (byte[] virtualMac : virtualMacs) {
            if (mac[0] == virtualMac[0] && mac[1] == virtualMac[1] && mac[2] == virtualMac[2]) {
                return true;
            }
        }

        return false;
    }

    /**
     * Valida si una dirección MAC tiene el formato correcto
     * @param mac dirección MAC a validar
     * @return true si el formato es válido
     */
    public static boolean isValidMacAddress(String mac) {
        if (mac == null || mac.isEmpty()) return false;

        // Formato: XX-XX-XX-XX-XX-XX o XX:XX:XX:XX:XX:XX
        String macPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
        return mac.matches(macPattern);
    }

    /**
     * Normaliza una dirección MAC al formato XX-XX-XX-XX-XX-XX
     * @param mac dirección MAC
     * @return dirección MAC normalizada
     */
    public static String normalizeMacAddress(String mac) {
        if (mac == null || mac.isEmpty()) return mac;

        // Remover caracteres no válidos y convertir a mayúsculas
        String normalized = mac.toUpperCase()
                .replaceAll("[^0-9A-F]", "");

        // Agregar guiones cada 2 caracteres
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < normalized.length(); i += 2) {
            if (i > 0) sb.append("-");
            sb.append(normalized.substring(i, Math.min(i + 2, normalized.length())));
        }

        return sb.toString();
    }
}
