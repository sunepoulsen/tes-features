package dk.sunepoulsen.tes.features.deployment

import dk.sunepoulsen.tes.features.integrator.FeaturesTestsIntegrator
import dk.sunepoulsen.tes.io.resources.PropertiesResource
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient
import dk.sunepoulsen.tes.sut.engine.providers.SystemUnderTestProvider
import dk.sunepoulsen.tes.sut.engine.services.SutHttpService

trait FeaturesTestsIntegratorProvider implements SystemUnderTestProvider {

    private FeaturesTestsIntegrator featuresTestsIntegratorInstance = null

    FeaturesTestsIntegrator featuresTestsIntegrator() {
        if (featuresTestsIntegratorInstance != null) {
            return featuresTestsIntegratorInstance
        }

        PropertiesResource propertiesResource = new PropertiesResource(FeaturesDeployment.class.getResourceAsStream("/features-deployment.properties"))

        SutHttpService featuresService = sut().findService(propertiesResource.getProperty('features.service.key'), SutHttpService).orElseThrow(() ->
            new IllegalStateException("Service '${propertiesResource.getProperty('features.service.key')}' is not deployed")
        )
        TechEasySolutionsClient client = new TechEasySolutionsClient(featuresService.baseUrl(8080), clientConfig())

        featuresTestsIntegratorInstance = new FeaturesTestsIntegrator(client)
        return featuresTestsIntegratorInstance
    }

}
