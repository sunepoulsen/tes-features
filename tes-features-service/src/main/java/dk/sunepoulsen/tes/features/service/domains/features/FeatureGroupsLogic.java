package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.FeatureGroup;
import dk.sunepoulsen.tes.features.service.domains.persistence.FeatureGroupPersistence;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity;
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
class FeatureGroupsLogic {

    private final FeatureGroupTransformations featureGroupTransformations;
    private final FeatureGroupPersistence featureGroupPersistence;

    @Async("logicExecutor")
    @Transactional
    CompletableFuture<FeatureGroup> getFeatureGroup(final String key) {
        try {
            Optional<FeatureGroupEntity> entity = featureGroupPersistence.getFeatureGroup(key);

            return entity
                .map(featureGroupEntity ->
                    CompletableFuture.completedFuture(featureGroupTransformations.toModel(featureGroupEntity))
                )
                .orElseGet(() ->
                    CompletableFuture.failedFuture(new ApiNotFoundException("key", "No feature group exists with key: " + key))
                );
        } catch (Exception ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

}
