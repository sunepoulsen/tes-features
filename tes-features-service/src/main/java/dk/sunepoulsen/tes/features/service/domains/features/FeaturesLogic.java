package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.*;
import dk.sunepoulsen.tes.features.service.domains.persistence.FeatureGroupPersistence;
import dk.sunepoulsen.tes.features.service.domains.persistence.FeaturePersistence;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureActivationEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity;
import dk.sunepoulsen.tes.rest.models.NoContent;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Logic for features.
 */
@Service
@RequiredArgsConstructor
class FeaturesLogic {

    private final FeatureGroupTransformations featureGroupTransformations;
    private final FeatureTransformations featureTransformations;
    private final FeatureGroupPersistence featureGroupPersistence;
    private final FeaturePersistence featurePersistence;
    private final FeatureActivationTransformations featureActivationTransformations;

    @Async("logicExecutor")
    CompletableFuture<RegisterFeatureGroup> registerFeatures(RegisterFeatureGroup featureGroup) {
        try {
            FeatureGroupEntity entity = featureGroupTransformations.toEntity(featureGroup);
            entity = featureGroupPersistence.registerFeatureGroup(entity);

            return CompletableFuture.completedFuture(featureGroupTransformations.toRegisterModel(entity));
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    @Async("logicExecutor")
    CompletableFuture<EnvelopeFeature> getFeatures(final String featureGroupKey) {
        try {
            List<FeatureEntity> entities = featurePersistence.getFeatures(featureGroupKey);

            EnvelopeFeature features = new EnvelopeFeature();
            features.setResults(entities.stream()
                .map(featureTransformations::toFeatureModel)
                .toList()
            );

            return CompletableFuture.completedFuture(features);
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    @Async("logicExecutor")
    CompletableFuture<Feature> getFeature(final String featureGroupKey, final String featureKey) {
        try {
            Optional<FeatureEntity> featureEntity = featurePersistence.getFeature(featureGroupKey, featureKey);

            return featureEntity
                .map(entity -> CompletableFuture.completedFuture(featureTransformations.toFeatureModel(entity)))
                .orElseGet(() ->
                    CompletableFuture.failedFuture(new ResourceNotFoundException("No feature exists with the given keys"))
                );
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    @Async("logicExecutor")
    CompletableFuture<Feature> patchFeature(final String featureGroupKey, final String featureKey, final Feature newValues) {
        try {
            return featurePersistence.patchFeature(featureGroupKey, featureKey, featureTransformations.toPatchEntity(newValues))
                .map(featureEntity ->
                    CompletableFuture.completedFuture(featureTransformations.toFeatureModel(featureEntity))
                )
                .orElseGet(() ->
                    CompletableFuture.failedFuture(new ResourceNotFoundException("No feature with feature group '" + featureGroupKey + "' and feature '" + featureKey + "' exists"))
                );
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    @Async("logicExecutor")
    CompletableFuture<NoContent> deleteFeature(final String featureGroupKey, final String featureKey) {
        try {
            featurePersistence.deleteFeature(featureGroupKey, featureKey);
            return CompletableFuture.completedFuture(new NoContent());
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    /**
     * Creates a new activation for the given feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @param newActivation   the activation to create
     * @return a {@link CompletableFuture} with the created activation
     */
    @Async("logicExecutor")
    CompletableFuture<FeatureActivation> createActivation(final String featureGroupKey, final String featureKey, final FeatureActivation newActivation) {
        try {
            FeatureActivationEntity activationEntity = featureActivationTransformations.toEntity(newActivation);
            activationEntity = featurePersistence.createActivation(featureGroupKey, featureKey, activationEntity);

            return CompletableFuture.completedFuture(featureActivationTransformations.toModel(activationEntity));
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    /**
     * Returns a list of all activations for the given feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey the feature key
     * @return a {@link CompletableFuture} with the activations
     */
    @Async("logicExecutor")
    CompletableFuture<EnvelopeFeatureActivation> getActivations(final String featureGroupKey, final String featureKey) {
        try {
            List<FeatureActivationEntity> entities = featurePersistence.getActivations(featureGroupKey, featureKey);

            EnvelopeFeatureActivation activations = new EnvelopeFeatureActivation();
            activations.setResults(entities.stream()
                .map(featureActivationTransformations::toModel)
                .toList()
            );

            return CompletableFuture.completedFuture(activations);
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

}
