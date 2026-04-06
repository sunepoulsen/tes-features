package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureActivation;
import dk.sunepoulsen.tes.features.model.EnvelopeFeatureGroup;
import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.features.model.FeatureGroup;
import dk.sunepoulsen.tes.features.service.domains.persistence.FeatureGroupPersistence;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupActivationEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity;
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiNotFoundException;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Logic for feature groups.
 */
@Service
@RequiredArgsConstructor
class FeatureGroupsLogic {

    private final FeatureGroupTransformations featureGroupTransformations;
    private final FeatureGroupPersistence featureGroupPersistence;
    private final FeatureGroupActivationTransformations featureGroupActivationTransformations;

    @Async("logicExecutor")
    CompletableFuture<EnvelopeFeatureGroup> getFeatureGroups() {
        try {
            List<FeatureGroupEntity> entities = featureGroupPersistence.getFeatureGroups();

            EnvelopeFeatureGroup groups = new EnvelopeFeatureGroup();
            groups.setResults(entities.stream()
                .map(featureGroupTransformations::toFeatureGroupModel)
                .toList()
            );

            return CompletableFuture.completedFuture(groups);
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    @Async("logicExecutor")
    CompletableFuture<FeatureGroup> getFeatureGroup(final String key) {
        try {
            Optional<FeatureGroupEntity> entity = featureGroupPersistence.getFeatureGroup(key);

            return entity
                .map(featureGroupEntity ->
                    CompletableFuture.completedFuture(featureGroupTransformations.toFeatureGroupModel(featureGroupEntity))
                )
                .orElseGet(() ->
                    CompletableFuture.failedFuture(new ApiNotFoundException("key", "No feature group exists with key: " + key))
                );
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    @Async("logicExecutor")
    CompletableFuture<FeatureGroup> patchFeatureGroup(final String key, final FeatureGroup newValues) {
        try {
            return featureGroupPersistence.patchFeatureGroup(key, featureGroupTransformations.toPatchEntity(newValues))
                .map(featureGroupEntity ->
                    CompletableFuture.completedFuture(featureGroupTransformations.toFeatureGroupModel(featureGroupEntity))
                )
                .orElseGet(() ->
                    CompletableFuture.failedFuture(new ResourceNotFoundException("key", "No feature group exists with key: " + key))
                );
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    @Async("logicExecutor")
    CompletableFuture<String> deleteFeatureGroup(final String key) {
        try {
            featureGroupPersistence.deleteFeatureGroup(key);
            return CompletableFuture.completedFuture("{}");
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    /**
     * Creates a new activation for the given feature group.
     *
     * @param key           the feature group key
     * @param newActivation the activation to create
     * @return a {@link CompletableFuture} with the created activation
     */
    @Async("logicExecutor")
    CompletableFuture<FeatureActivation> createActivation(final String key, final FeatureActivation newActivation) {
        try {
            FeatureGroupActivationEntity activationEntity = featureGroupActivationTransformations.toEntity(newActivation);
            activationEntity = featureGroupPersistence.createActivation(key, activationEntity);

            return CompletableFuture.completedFuture(featureGroupActivationTransformations.toModel(activationEntity));
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    /**
     * Returns a list of all activations for the given feature group.
     *
     * @param key the feature group key
     * @return a {@link CompletableFuture} with the activations
     */
    @Async("logicExecutor")
    CompletableFuture<EnvelopeFeatureActivation> getActivations(final String key) {
        try {
            List<FeatureGroupActivationEntity> entities = featureGroupPersistence.getActivations(key);

            EnvelopeFeatureActivation activations = new EnvelopeFeatureActivation();
            activations.setResults(entities.stream()
                .map(featureGroupActivationTransformations::toModel)
                .toList()
            );

            return CompletableFuture.completedFuture(activations);
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    /**
     * Returns a specific activation for the given feature group.
     *
     * @param key          the feature group key
     * @param activationId the activation id
     * @return a {@link CompletableFuture} with the activation
     */
    @Async("logicExecutor")
    CompletableFuture<FeatureActivation> getActivation(final String key, final Long activationId) {
        try {
            Optional<FeatureGroupActivationEntity> entity = featureGroupPersistence.getActivation(key, activationId);

            return entity
                .map(activationEntity ->
                    CompletableFuture.completedFuture(featureGroupActivationTransformations.toModel(activationEntity))
                )
                .orElseGet(() ->
                    CompletableFuture.failedFuture(new ApiNotFoundException("activation_id", "No activation exists with id: " + activationId))
                );
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }
}
