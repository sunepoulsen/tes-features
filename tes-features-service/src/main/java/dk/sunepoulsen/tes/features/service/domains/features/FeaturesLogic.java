package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.EnvelopeFeature;
import dk.sunepoulsen.tes.features.model.Feature;
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup;
import dk.sunepoulsen.tes.features.service.domains.persistence.FeatureGroupPersistence;
import dk.sunepoulsen.tes.features.service.domains.persistence.FeaturePersistence;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
class FeaturesLogic {

    private final FeatureGroupTransformations featureGroupTransformations;
    private final FeatureTransformations featureTransformations;
    private final FeatureGroupPersistence featureGroupPersistence;
    private final FeaturePersistence featurePersistence;

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

}
