package dk.sunepoulsen.tes.features.integrator;

import dk.sunepoulsen.tes.features.model.FeatureGroup;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsBackendIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;
import io.reactivex.rxjava3.core.Single;

public class FeaturesIntegrator extends TechEasySolutionsBackendIntegrator {

    public FeaturesIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    public Single<FeatureGroup> registerFeatures(FeatureGroup featureGroup) {
        return Single.fromFuture(this.httpClient.put("/features", featureGroup, FeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

}
