package dk.sunepoulsen.tes.features.integrator;

import dk.sunepoulsen.tes.features.model.FeatureGroup;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsBackendIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;
import io.reactivex.rxjava3.core.Single;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FeaturesIntegrator extends TechEasySolutionsBackendIntegrator {

    static String FEATURE_ENDPOINT_PATH = "/features";
    static String FEATURE_GROUPS_ENDPOINT_PATH = "/feature-groups";

    public FeaturesIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    public Single<FeatureGroup> registerFeatures(FeatureGroup featureGroup) {
        return Single.fromFuture(this.httpClient.put(FEATURE_ENDPOINT_PATH, featureGroup, FeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<FeatureGroup> getFeatureGroup(final String key) {
        String url = String.format("%s/%s",
            FEATURE_GROUPS_ENDPOINT_PATH,
            URLEncoder.encode(key, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.get(url, FeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

}
