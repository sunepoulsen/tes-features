package dk.sunepoulsen.tes.features.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.nimbusds.jose.JOSEException;
import dk.sunepoulsen.tes.keycloak.wiremock.KeycloakJwtUser;
import dk.sunepoulsen.tes.keycloak.wiremock.KeycloakWiremock;
import dk.sunepoulsen.tes.wiremock.deployment.WiremockServerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class LocalContainersConfig {

    @Bean
    JwtDecoder jwtDecoder(OAuth2ResourceServerProperties properties) {
        return JwtDecoders.fromIssuerLocation(
            properties.getJwt().getIssuerUri()
        );
    }

    @Bean
    @RestartScope
    GenericContainer<?> wiremockContainer() {
        return new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:3.13.2-alpine"))
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/__admin/health").forStatusCode(200));
    }

    @Bean
    DynamicPropertyRegistrar configureWiremockContainer(GenericContainer<?> wiremockContainer) throws JOSEException {
        WiremockServerProperties wiremockServerProperties = WiremockServerProperties.builder()
            .host(wiremockContainer.getHost())
            .port(wiremockContainer.getMappedPort(8080))
            .build();
        WireMock.configureFor(wiremockServerProperties.getHost(), wiremockServerProperties.getPort());

        KeycloakWiremock keycloakWiremock = new KeycloakWiremock(wiremockServerProperties, "keycloak", "tes-foundation", "tes-foundation-kid");
        keycloakWiremock.createStubs();

        log.info("Authenticated user: {}", keycloakWiremock.keycloakJwt()
            .createAuthorizationToken(UUID.randomUUID().toString(), "tes-foundation",
                KeycloakJwtUser.builder()
                    .clientId("tes-foundation")
                    .username("jennifer")
                    .roles(List.of("register-features", "admin-features"))
                    .build()
            )
        );

        return registry -> {
            registry.add(
                "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> "http://%s:%s/keycloak/realms/tes-foundation".formatted(
                    wiremockServerProperties.getHost(),
                    wiremockServerProperties.getPort()
                )
            );

            registry.add(
                "oauth2.security.keycloak.client-id",
                () -> "tes-foundation"
            );
        };
    }

}
