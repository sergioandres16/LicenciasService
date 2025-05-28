package org.saeta.licenciasservice.service;

import org.saeta.licenciasservice.entity.User;
import org.saeta.licenciasservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Crea un usuario en la base de datos (registro).
     * @throws RuntimeException si el username o email ya existen.
     */
    public User registerUser(String username, String email, String rawPassword) {
        if(userRepository.existsByUsername(username)) {
            throw new RuntimeException("El username ya existe: " + username);
        }
        if(userRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya existe: " + email);
        }

        String hashed = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(hashed);

        return userRepository.save(user);
    }

    /**
     * Busca un usuario por username.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Comprueba las credenciales para el login.
     */
    public User login(String usernameOrEmail, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
            if (!userOpt.isPresent()) {
                throw new RuntimeException("Usuario no encontrado");
            }
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Contraseña inválida");
        }
        return user;
    }
}