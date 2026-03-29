package dk.sunepoulsen.tes.features.integrator;

import dk.sunepoulsen.tes.features.model.*;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsBackendIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;
import io.reactivex.rxjava3.core.Single;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FeaturesIntegrator extends TechEasySolutionsBackendIntegrator {

    public static final String REGISTER_FEATURES_ENDPOINT_PATH = "/features";
    public static final String FEATURE_GROUPS_ENDPOINT_PATH = "/groups";
    public static final String FEATURES_ENDPOINT_PATH = FEATURE_GROUPS_ENDPOINT_PATH + "/%s/features";
    public static final String FEATURE_ENDPOINT_PATH = FEATURE_GROUPS_ENDPOINT_PATH + "/%s/features/%s";
    private static final String URI_PATH_WITH_ID_FORMAT = "%s/%s";

    public FeaturesIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    public Single<RegisterFeatureGroup> registerFeatures(RegisterFeatureGroup featureGroup) {
        return Single.fromFuture(this.httpClient.put(REGISTER_FEATURES_ENDPOINT_PATH, featureGroup, RegisterFeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<EnvelopeFeatureGroup> getFeatureGroups() {
        return Single.fromFuture(this.httpClient.get(FEATURE_GROUPS_ENDPOINT_PATH, EnvelopeFeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<FeatureGroup> getFeatureGroup(final String key) {
        String url = String.format(URI_PATH_WITH_ID_FORMAT,
            FEATURE_GROUPS_ENDPOINT_PATH,
            URLEncoder.encode(key, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.get(url, FeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<FeatureGroup> patchFeatureGroup(final String key, final FeatureGroup featureGroup) {
        String url = String.format(URI_PATH_WITH_ID_FORMAT,
            FEATURE_GROUPS_ENDPOINT_PATH,
            URLEncoder.encode(key, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.patch(url, featureGroup, FeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<String> deleteFeatureGroup(final String key) {
        String url = String.format(URI_PATH_WITH_ID_FORMAT,
            FEATURE_GROUPS_ENDPOINT_PATH,
            URLEncoder.encode(key, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.delete(url))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<EnvelopeFeature> getFeatures(final String featureGroupKey) {
        String url = String.format(FEATURES_ENDPOINT_PATH,
            URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.get(url, EnvelopeFeature.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<Feature> getFeature(final String featureGroupKey, final String featureKey) {
        String url = String.format(FEATURE_ENDPOINT_PATH,
            URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8),
            URLEncoder.encode(featureKey, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.get(url, Feature.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<Feature> patchFeature(final String featureGroupKey, final String featureKey, final Feature feature) {
        String url = String.format(FEATURE_ENDPOINT_PATH,
            URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8),
            URLEncoder.encode(featureKey, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.patch(url, feature, Feature.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    public Single<String> deleteFeature(final String featureGroupKey, final String featureKey) {
        String url = String.format(FEATURE_ENDPOINT_PATH,
            URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8),
            URLEncoder.encode(featureKey, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.delete(url))
            .onErrorResumeNext(this::mapClientExceptions);
    }

}
