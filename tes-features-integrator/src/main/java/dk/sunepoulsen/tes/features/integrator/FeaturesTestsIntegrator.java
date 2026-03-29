package dk.sunepoulsen.tes.features.integrator;

import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsBackendIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;
import dk.sunepoulsen.tes.rest.models.NoContent;
import io.reactivex.rxjava3.core.Single;

public class FeaturesTestsIntegrator extends TechEasySolutionsBackendIntegrator {

    public FeaturesTestsIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    public Single<NoContent> deletePersistence() {
        return Single.fromFuture(this.httpClient.delete("/tests/persistence"))
            .onErrorResumeNext(this::mapClientExceptions);
    }

}
