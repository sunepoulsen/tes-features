package dk.sunepoulsen.tes.features.service.domains.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
public class SecurityConfig {
    @Value("${test.endpoints.enabled}")
    private Boolean testEndpointsEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);

        http.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
            if (!Boolean.TRUE.equals(testEndpointsEnabled)) {
                authorizationManagerRequestMatcherRegistry
                    .requestMatchers(mvcMatcherBuilder.pattern("/tests/**"))
                    .denyAll();
            }

            authorizationManagerRequestMatcherRegistry
                .requestMatchers(mvcMatcherBuilder.pattern("/**"))
                .permitAll();
            }
        );

        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
