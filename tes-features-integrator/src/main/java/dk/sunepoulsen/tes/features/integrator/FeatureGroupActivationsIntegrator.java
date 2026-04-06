package dk.sunepoulsen.tes.features.integrator;

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureActivation;
import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.rest.integrations.AbstractIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;
import io.reactivex.rxjava3.core.Single;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FeatureGroupActivationsIntegrator extends AbstractIntegrator {

    public static final String FEATURE_GROUP_ACTIVATIONS_ENDPOINT_PATH = FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH + "/%s/activations";

    public FeatureGroupActivationsIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    /**
     * Creates a new activation for a feature group.
     *
     * @param featureGroupKey   the feature group key
     * @param featureActivation the activation to create
     * @return a {@link Single} with the created activation
     */
    public Single<FeatureActivation> createFeatureGroupActivation(final String featureGroupKey, final FeatureActivation featureActivation) {
        String url = String.format(FEATURE_GROUP_ACTIVATIONS_ENDPOINT_PATH, URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8));

        return Single.fromFuture(this.httpClient.post(url, featureActivation, FeatureActivation.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    /**
     * Returns all activations for a feature group.
     *
     * @param featureGroupKey the feature group key
     * @return a {@link Single} with the activations
     */
    public Single<EnvelopeFeatureActivation> getFeatureGroupActivations(final String featureGroupKey) {
        String url = String.format(FEATURE_GROUP_ACTIVATIONS_ENDPOINT_PATH, URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8));

        return Single.fromFuture(this.httpClient.get(url, EnvelopeFeatureActivation.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

}
