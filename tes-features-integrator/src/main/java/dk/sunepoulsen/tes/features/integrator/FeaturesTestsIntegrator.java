package dk.sunepoulsen.tes.features.integrator;

import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsBackendIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;
import io.reactivex.rxjava3.core.Completable;

public class FeaturesTestsIntegrator extends TechEasySolutionsBackendIntegrator {

    public FeaturesTestsIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    public Completable deletePersistence() {
        return Completable.fromCompletionStage(this.httpClient.delete("/tests/persistence"))
            .onErrorResumeNext(this::mapClientExceptionsAsCompletable);
    }

}
