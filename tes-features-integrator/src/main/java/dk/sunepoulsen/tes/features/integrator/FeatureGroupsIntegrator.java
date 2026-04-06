package dk.sunepoulsen.tes.features.integrator;

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureGroup;
import dk.sunepoulsen.tes.features.model.FeatureGroup;
import dk.sunepoulsen.tes.rest.integrations.AbstractIntegrator;
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient;
import dk.sunepoulsen.tes.rest.models.NoContent;
import io.reactivex.rxjava3.core.Single;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FeatureGroupsIntegrator extends AbstractIntegrator {

    public static final String FEATURE_GROUPS_ENDPOINT_PATH = "/groups";
    private static final String URI_PATH_WITH_ID_FORMAT = "%s/%s";

    public FeatureGroupsIntegrator(TechEasySolutionsClient httpClient) {
        super(httpClient);
    }

    public FeatureGroupActivationsIntegrator activations() {
        return new FeatureGroupActivationsIntegrator(this.httpClient);
    }

    /**
     * Returns all feature groups.
     *
     * @return a {@link Single} with all feature groups
     */
    public Single<EnvelopeFeatureGroup> getFeatureGroups() {
        return Single.fromFuture(this.httpClient.get(FEATURE_GROUPS_ENDPOINT_PATH, EnvelopeFeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    /**
     * Returns a feature group.
     *
     * @param key the feature group key
     * @return a {@link Single} with the feature group
     */
    public Single<FeatureGroup> getFeatureGroup(final String key) {
        String url = String.format(URI_PATH_WITH_ID_FORMAT,
            FEATURE_GROUPS_ENDPOINT_PATH,
            URLEncoder.encode(key, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.get(url, FeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    /**
     * Patches a feature group.
     *
     * @param key          the feature group key
     * @param featureGroup the feature group values to patch
     * @return a {@link Single} with the patched feature group
     */
    public Single<FeatureGroup> patchFeatureGroup(final String key, final FeatureGroup featureGroup) {
        String url = String.format(URI_PATH_WITH_ID_FORMAT,
            FEATURE_GROUPS_ENDPOINT_PATH,
            URLEncoder.encode(key, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.patch(url, featureGroup, FeatureGroup.class))
            .onErrorResumeNext(this::mapClientExceptions);
    }

    /**
     * Deletes a feature group.
     *
     * @param key the feature group key
     * @return a {@link Single} with no content
     */
    public Single<NoContent> deleteFeatureGroup(final String key) {
        String url = String.format(URI_PATH_WITH_ID_FORMAT,
            FEATURE_GROUPS_ENDPOINT_PATH,
            URLEncoder.encode(key, StandardCharsets.UTF_8)
        );

        return Single.fromFuture(this.httpClient.delete(url))
            .onErrorResumeNext(this::mapClientExceptions);
    }

}
