package org.saeta.licenciasservice.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de la base de datos
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "org.saeta.licenciasservice.repository")
@EntityScan(basePackages = "org.saeta.licenciasservice.entity")
public class DatabaseConfig {
    // La configuración adicional se maneja a través de application.properties
}
