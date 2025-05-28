package org.saeta.licenciasservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String nombre;  // Para el nombre de usuario
    private String correo;  // Para el correo del usuario
}