package dk.sunepoulsen.tes.features.integrator;

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureGroup;
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsBackendIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;
import io.reactivex.rxjava3.core.Single;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FeaturesIntegrator extends TechEasySolutionsBackendIntegrator {

    public static final String FEATURE_ENDPOINT_PATH = "/features";
    public static final String FEATURE_GROUPS_ENDPOINT_PATH = "/groups";

    public FeaturesIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    public Single<RegisterFeatureGroup> registerFeatures(RegisterFeatureGroup featureGroup) {
        return Single.fromFuture(this.httpClient.put(FEATURE_ENDPOINT_PATH, featureGroup, RegisterFeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<EnvelopeFeatureGroup> getFeatureGroups() {
        return Single.fromFuture(this.httpClient.get(FEATURE_GROUPS_ENDPOINT_PATH, EnvelopeFeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<RegisterFeatureGroup> getFeatureGroup(final String key) {
        String url = String.format("%s/%s",
            FEATURE_GROUPS_ENDPOINT_PATH,
            URLEncoder.encode(key, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.get(url, RegisterFeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

}
