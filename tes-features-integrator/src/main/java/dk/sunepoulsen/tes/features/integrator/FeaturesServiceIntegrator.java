package dk.sunepoulsen.tes.features.integrator;

import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsBackendIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;

/**
 * Integrator for features.
 */
public class FeaturesServiceIntegrator extends TechEasySolutionsBackendIntegrator {

    /**
     * Constructs a new integrator.
     *
     * @param httpClient the client to use for HTTP requests
     */
    public FeaturesServiceIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    public FeatureGroupsIntegrator featureGroups() {
        return new FeatureGroupsIntegrator(this.httpClient);
    }

    public FeaturesIntegrator features() {
        return new FeaturesIntegrator(this.httpClient);
    }

}
