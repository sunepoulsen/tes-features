package dk.sunepoulsen.tes.features.integrator;

import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsBackendIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;
import io.reactivex.rxjava3.core.Single;

public class FeaturesTestsIntegrator extends TechEasySolutionsBackendIntegrator {

    public FeaturesTestsIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    public Single<Class<Void>> deletePersistence() {
        return Single.fromFuture(this.httpClient.delete("/tests/persistence"))
            .map(s -> Void.TYPE)
            .onErrorResumeNext(this::mapClientExceptions);
    }

}
