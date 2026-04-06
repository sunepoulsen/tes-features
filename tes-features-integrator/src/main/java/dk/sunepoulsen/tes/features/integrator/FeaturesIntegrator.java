package dk.sunepoulsen.tes.features.integrator;

import dk.sunepoulsen.tes.features.model.EnvelopeFeature;
import dk.sunepoulsen.tes.features.model.Feature;
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup;
import dk.sunepoulsen.tes.rest.integrations.AbstractIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;
import dk.sunepoulsen.tes.rest.models.NoContent;
import io.reactivex.rxjava3.core.Single;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FeaturesIntegrator extends AbstractIntegrator {

    public static final String REGISTER_FEATURES_ENDPOINT_PATH = "/features";
    public static final String FEATURES_ENDPOINT_PATH = FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH + "/%s/features";
    public static final String FEATURE_ENDPOINT_PATH = FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH + "/%s/features/%s";

    public FeaturesIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    public FeatureActivationsIntegrator activations() {
        return new FeatureActivationsIntegrator(this.httpClient);
    }

    /**
     * Registers features.
     *
     * @param featureGroup the feature group to register
     * @return a {@link Single} with the registered feature group
     */
    public Single<RegisterFeatureGroup> registerFeatures(RegisterFeatureGroup featureGroup) {
        return Single.fromFuture(this.httpClient.put(REGISTER_FEATURES_ENDPOINT_PATH, featureGroup, RegisterFeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    /**
     * Returns all features for a feature group.
     *
     * @param featureGroupKey the feature group key
     * @return a {@link Single} with the features
     */
    public Single<EnvelopeFeature> getFeatures(final String featureGroupKey) {
        String url = String.format(FEATURES_ENDPOINT_PATH,
            URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.get(url, EnvelopeFeature.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    /**
     * Returns a feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey the feature key
     * @return a {@link Single} with the feature
     */
    public Single<Feature> getFeature(final String featureGroupKey, final String featureKey) {
        String url = String.format(FEATURE_ENDPOINT_PATH,
            URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8),
            URLEncoder.encode(featureKey, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.get(url, Feature.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    /**
     * Patches a feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey the feature key
     * @param feature the feature values to patch
     * @return a {@link Single} with the patched feature
     */
    public Single<Feature> patchFeature(final String featureGroupKey, final String featureKey, final Feature feature) {
        String url = String.format(FEATURE_ENDPOINT_PATH,
            URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8),
            URLEncoder.encode(featureKey, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.patch(url, feature, Feature.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    /**
     * Deletes a feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey the feature key
     * @return a {@link Single} with no content
     */
    public Single<NoContent> deleteFeature(final String featureGroupKey, final String featureKey) {
        String url = String.format(FEATURE_ENDPOINT_PATH,
            URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8),
            URLEncoder.encode(featureKey, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.delete(url))
            .onErrorResumeNext(this::mapClientExceptions);
    }

}
