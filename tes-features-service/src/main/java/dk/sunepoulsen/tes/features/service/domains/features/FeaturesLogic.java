package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup;
import dk.sunepoulsen.tes.features.service.domains.persistence.FeatureGroupPersistence;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
class FeaturesLogic {

    private final FeatureGroupTransformations featureGroupTransformations;
    private final FeatureGroupPersistence featureGroupPersistence;

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

}
