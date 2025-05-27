package org.saeta.licenciasservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger/OpenAPI
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name:LicenciasService}")
    private String appName;

    @Value("${spring.application.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("/");
        server.setDescription("Servidor por defecto");

        Contact contact = new Contact();
        contact.setName("Soporte Técnico");
        contact.setEmail("soporte@saeta.pe");

        License license = new License();
        license.setName("Propietario");
        license.setUrl("https://saeta.pe");

        Info info = new Info()
                .title(appName + " API")
                .version(appVersion)
                .description("Servicio REST para validación de licencias por dirección MAC")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}