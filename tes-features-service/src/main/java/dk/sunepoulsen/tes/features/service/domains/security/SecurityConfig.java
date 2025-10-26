package dk.sunepoulsen.tes.features.service.domains.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
public class SecurityConfig {
    @Value("${test.endpoints.enabled}")
    private Boolean testEndpointsEnabled;

    @Value("${test.csrf.disabled:false}")
    private Boolean testCsrfDisabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> {
            if (!Boolean.TRUE.equals(testEndpointsEnabled)) {
                log.info("Denying access to test endpoints: /tests/**");

                auth
                    .requestMatchers("/tests/**")
                    .denyAll();
            } else {
                log.info("Activating access to test endpoints: /tests/**");
            }

            auth
                .requestMatchers("/**")
                .permitAll();
            }
        );

        if (Boolean.TRUE.equals(testCsrfDisabled)) {
            log.info("Disabling CSRF!");
            http.csrf(AbstractHttpConfigurer::disable);
        }

        return http.build();
    }
}
