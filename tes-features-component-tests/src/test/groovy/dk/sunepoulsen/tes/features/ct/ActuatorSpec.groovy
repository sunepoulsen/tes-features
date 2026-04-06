package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.rest.models.monitoring.*
import spock.lang.Specification

class ActuatorSpec extends Specification implements FeaturesServiceIntegratorProvider {

    void "GET /actuator/health returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'Call GET /actuator/health'
            ServiceHealth result = featuresServiceIntegrator().health().blockingGet()

        then: 'Verify health body'
            result == new ServiceHealth(
                status: ServiceHealthStatusCode.UP
            )
    }

    void "GET /actuator/info returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'Call GET /actuator/info'
            ServiceInfo result = featuresServiceIntegrator().info().blockingGet()

        then: 'Verify info body'
            result == new ServiceInfo(
                app: new ServiceInfoApp(
                    name: 'tes-features',
                    version: '1.0.0-SNAPSHOT',
                    service: new ServiceInfoService(
                        name: 'tes-features-service'
                    )
                )
            )
    }

    void "GET /actuator/env returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'Call GET /actuator/env'
            Map result = featuresServiceIntegrator().env().blockingGet()

        then: 'Verify env body'
            result.activeProfiles.sort() == ['ct', 'tests']
    }

}
