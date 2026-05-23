package dk.sunepoulsen.tes.features.st

import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.lang.SystemEnvironment
import dk.sunepoulsen.tes.utils.ProcessExecutor
import dk.sunepoulsen.tes.wiremock.deployment.WiremockIntegratorProvider
import dk.sunepoulsen.tes.wiremock.deployment.WiremockServerEnvironment
import dk.sunepoulsen.tes.wiremock.deployment.WiremockServerProperties
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class GatlingSpec extends Specification implements FeaturesServiceIntegratorProvider, WiremockIntegratorProvider {

    void "Executing Gatling simulations"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and:
            Integer featuresServicePort = featuresService().container().getMappedPort(8080)

        and:
            WiremockServerProperties wiremockServerProperties = wiremockIntegrator()

        when: 'Executing Gatling Simalation'
            log.debug('Current directory: {}', new File('.').toPath().toAbsolutePath())

            ProcessExecutor executor = new ProcessExecutor({ ProcessBuilder it ->
                it.environment()
                    .with {
                        SystemEnvironment.putJavaHome(it)
                        put('FEATURES_SERVICE_URL', featuresService().baseUrl().toString())

                        WiremockServerEnvironment.storeEnvironmentVariables(wiremockServerProperties, it)
                    }
            })
            int exitCode = executor.execute('../gradlew', ':tes-features-stress-test:gatlingRun', '--simulation', 'dk.sunepoulsen.tes.features.st.FeaturesServiceSimulation', '--non-interactive')
            log.info("")

        then:
            exitCode == 0
    }

}
