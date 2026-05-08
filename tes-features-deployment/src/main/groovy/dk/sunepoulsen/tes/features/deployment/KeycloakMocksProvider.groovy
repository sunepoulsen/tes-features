package dk.sunepoulsen.tes.features.deployment

import dk.sunepoulsen.tes.keycloak.wiremock.KeycloakJwt
import dk.sunepoulsen.tes.keycloak.wiremock.KeycloakWiremock
import dk.sunepoulsen.tes.sut.engine.providers.SystemUnderTestProvider
import dk.sunepoulsen.tes.wiremock.deployment.WiremockIntegratorProvider
import dk.sunepoulsen.tes.wiremock.deployment.WiremockServerProperties
import groovy.util.logging.Slf4j

@Slf4j
trait KeycloakMocksProvider implements SystemUnderTestProvider, WiremockIntegratorProvider {

    KeycloakJwt keycloakJwtIntegrator() {
        if (sut().getContext('keycloak.wiremock', KeycloakWiremock).empty) {
            log.debug('Initialize keycloak with keys and stubbing')
            WiremockServerProperties wiremockServerProperties = wiremockIntegrator()
            KeycloakWiremock keycloakWiremock = new KeycloakWiremock(wiremockServerProperties, 'keycloak', 'tes-foundation', 'tes-foundation-kid')
            keycloakWiremock.createStubs()

            sut().putContext('keycloak.wiremock', keycloakWiremock)
        }

        return sut().getContext('keycloak.wiremock', KeycloakWiremock).get().keycloakJwt()
    }

}
