package dk.sunepoulsen.tes.features.service.configuration;

import dk.sunepoulsen.tes.springboot.security.keycloak.HttpSecurityKeycloakCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("!ut")
public class OAuth2SecurityConfiguration {
    @Value("${oauth2.security.keycloak.client-id}")
    private String clientId;

    @Value("${oauth2.security.roles.register-role}")
    private String registerRole;

    @Value("${oauth2.security.roles.admin-role}")
    private String adminRole;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        new HttpSecurityKeycloakCustomizer()
            .customize(http, clientId, auth -> auth
                .requestMatchers(HttpMethod.PUT, "/features").hasRole(registerRole.toUpperCase())
                .requestMatchers("/groups/**").hasRole(adminRole.toUpperCase())
            );

        return http.build();
    }

}
