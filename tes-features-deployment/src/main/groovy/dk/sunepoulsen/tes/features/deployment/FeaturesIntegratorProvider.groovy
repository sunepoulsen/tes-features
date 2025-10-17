package dk.sunepoulsen.tes.features.deployment

import dk.sunepoulsen.tes.features.integrator.FeaturesIntegrator
import dk.sunepoulsen.tes.io.resources.PropertiesResource
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient
import dk.sunepoulsen.tes.sut.engine.providers.SystemUnderTestProvider
import dk.sunepoulsen.tes.sut.engine.services.SutHttpService
import dk.sunepoulsen.tes.sut.engine.services.SutService

trait FeaturesIntegratorProvider implements SystemUnderTestProvider {

    private FeaturesIntegrator featuresIntegratorInstance = null

    boolean isFeaturesServiceAvailable() {
        PropertiesResource propertiesResource = new PropertiesResource(FeaturesDeployment.class.getResourceAsStream('/features-deployment.properties'))

        return sut().findService(propertiesResource.getProperty('features.service.key'), SutService).orElseThrow(() ->
            new IllegalStateException("Service '${propertiesResource.getProperty('features.service.key')}' is not deployed")
        ).container().isHostAccessible()
    }

    FeaturesIntegrator featuresIntegrator() {
        if (featuresIntegratorInstance != null) {
            return featuresIntegratorInstance
        }

        PropertiesResource propertiesResource = new PropertiesResource(FeaturesDeployment.class.getResourceAsStream("/features-deployment.properties"))

        SutHttpService featuresService = sut().findService(propertiesResource.getProperty('features.service.key'), SutHttpService).orElseThrow(() ->
            new IllegalStateException("Service '${propertiesResource.getProperty('features.service.key')}' is not deployed")
        )
        TechEasySolutionsClient client = new TechEasySolutionsClient(featuresService.baseUrl(8080), clientConfig())

        featuresIntegratorInstance = new FeaturesIntegrator(client)
        return featuresIntegratorInstance
    }

}
