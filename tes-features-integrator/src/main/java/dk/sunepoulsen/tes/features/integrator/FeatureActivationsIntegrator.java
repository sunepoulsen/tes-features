package dk.sunepoulsen.tes.features.integrator;

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureActivation;
import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.rest.integrations.AbstractIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FeatureActivationsIntegrator extends AbstractIntegrator {

    public static final String FEATURE_ENDPOINT_ACTIVATIONS_ENDPOINT_PATH = FeaturesIntegrator.FEATURE_ENDPOINT_PATH + "/activations";

    public FeatureActivationsIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    /**
     * Creates a new activation for a feature.
     *
     * @param authorizationToken authorization token of the user performing the action.
     * @param featureGroupKey   the feature group key
     * @param featureKey        the feature key
     * @param featureActivation the activation to create
     * @return a {@link Single} with the created activation
     */
    public Single<FeatureActivation> createFeatureActivation(final String authorizationToken, final String featureGroupKey, final String featureKey, final FeatureActivation featureActivation) {
        String url = String.format(FEATURE_ENDPOINT_ACTIVATIONS_ENDPOINT_PATH,
            URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8),
            URLEncoder.encode(featureKey, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.post(url, authorizationToken, featureActivation, FeatureActivation.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    /**
     * Returns all activations for a feature.
     *
     * @param authorizationToken authorization token of the user performing the action.
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @return a {@link Single} with the activations
     */
    public Single<EnvelopeFeatureActivation> getFeatureActivations(final String authorizationToken, final String featureGroupKey, final String featureKey) {
        String url = String.format(FEATURE_ENDPOINT_ACTIVATIONS_ENDPOINT_PATH,
            URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8),
            URLEncoder.encode(featureKey, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.get(url, authorizationToken, EnvelopeFeatureActivation.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    /**
     * Returns a specific activation for a feature.
     *
     * @param authorizationToken authorization token of the user performing the action.
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @param activationId    the activation id
     * @return a {@link Single} with the activation
     */
    public Single<FeatureActivation> getFeatureActivation(final String authorizationToken, final String featureGroupKey, final String featureKey, final Long activationId) {
        String url = String.format(FEATURE_ENDPOINT_ACTIVATIONS_ENDPOINT_PATH + "/%s",
            URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8),
            URLEncoder.encode(featureKey, StandardCharsets.UTF_8),
            URLEncoder.encode(activationId.toString(), StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.get(url, authorizationToken, FeatureActivation.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    /**
     * Patches a specific activation for a given feature group.
     *
     * @param authorizationToken authorization token of the user performing the action.
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @param activationId    the activation id
     * @param newActivation   the activation to patch
     * @return the activation
     */
    public Single<FeatureActivation> patchFeatureActivation(final String authorizationToken, final String featureGroupKey, final String featureKey, final Long activationId, final FeatureActivation newActivation) {
        String url = String.format(FEATURE_ENDPOINT_ACTIVATIONS_ENDPOINT_PATH + "/%s",
            URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8),
            URLEncoder.encode(featureKey, StandardCharsets.UTF_8),
            URLEncoder.encode(activationId.toString(), StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.patch(url, authorizationToken, newActivation, FeatureActivation.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    /**
     * Delete a specific activation for a given feature group.
     *
     * @param authorizationToken authorization token of the user performing the action.
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @param activationId    the activation id
     * @return A {@code Completable} for the async operation.
     */
    public Completable deleteFeatureActivation(final String authorizationToken, final String featureGroupKey, final String featureKey, final Long activationId) {
        String url = String.format(FEATURE_ENDPOINT_ACTIVATIONS_ENDPOINT_PATH + "/%s",
            URLEncoder.encode(featureGroupKey, StandardCharsets.UTF_8),
            URLEncoder.encode(featureKey, StandardCharsets.UTF_8),
            URLEncoder.encode(activationId.toString(), StandardCharsets.UTF_8)
        );

        return Completable.fromCompletionStage(this.httpClient.delete(url, authorizationToken))
            .onErrorResumeNext(this::mapClientExceptionsAsCompletable);
    }

}
