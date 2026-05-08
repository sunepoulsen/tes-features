package dk.sunepoulsen.tes.features.deployment

import dk.sunepoulsen.tes.keycloak.wiremock.KeycloakJwtUser
import dk.sunepoulsen.tes.keycloak.wiremock.KeycloakWiremock
import dk.sunepoulsen.tes.wiremock.deployment.WiremockServerProperties

trait FeaturesMockUsers implements KeycloakMocksProvider {

    String featuresDefaultUser() {
        return keycloakJwtIntegrator().createAuthorizationToken(UUID.randomUUID().toString(), ['tes-foundation'], KeycloakJwtUser.builder()
            .clientId('tes-foundation')
            .username('user')
            .roles(['register-features', 'admin-features'])
            .build()
        )
    }

    String featuresUnknownUser() {
        return keycloakJwtIntegrator().createAuthorizationToken(UUID.randomUUID().toString(), ['tes-foundation'], KeycloakJwtUser.builder()
            .clientId('tes-foundation')
            .username('user')
            .build()
        )
    }

    String featuresHackerUser() {
        WiremockServerProperties wiremockServerProperties = wiremockIntegrator()
        KeycloakWiremock keycloakWiremock = new KeycloakWiremock(wiremockServerProperties, 'keycloak', 'tes-foundation', 'tes-foundation-kid')

        return keycloakWiremock.keycloakJwt().createAuthorizationToken(UUID.randomUUID().toString(), ['tes-foundation'], KeycloakJwtUser.builder()
            .clientId('tes-foundation')
            .username('user')
            .roles(['register-features', 'admin-features'])
            .build()
        )
    }

}
