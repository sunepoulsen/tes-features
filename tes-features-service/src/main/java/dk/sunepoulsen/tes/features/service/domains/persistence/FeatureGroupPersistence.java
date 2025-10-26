package dk.sunepoulsen.tes.features.service.domains.persistence;

import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureGroupPersistence {

    private final FeatureGroupRepository featureGroupRepository;
    private final FeaturePersistence featurePersistence;
    private final FeatureRepository featureRepository;

    public FeatureGroupEntity registerFeatureGroup(FeatureGroupEntity featureGroup) throws PersistenceException {
        verifyFeatures(featureGroup);

        List<FeatureEntity> features = featureGroup.getFeatures();

        FeatureGroupEntity entity = featureGroupRepository.findByKey(featureGroup.getKey()).orElseGet(() -> {
            log.info("Creating new feature group {}", featureGroup.getKey());

            featureGroup.setFeatures(null);
            return featureGroupRepository.save(featureGroup);
        });

        features.forEach(feature -> {
            feature.setFeatureGroup(entity);
            featurePersistence.registerFeature(feature);
        });

        entity.setFeatures(featureRepository.findByFeatureGroup(entity));
        return entity;
    }

    public Optional<FeatureGroupEntity> getFeatureGroup(String featureGroupKey) throws PersistenceException {
        return featureGroupRepository.findByKey(featureGroupKey);
    }

    private void verifyFeatures(FeatureGroupEntity featureGroup) throws PersistenceException {
        if (featureGroup.getFeatures() == null) {
            throw new PersistenceException("Features of feature group may not be null");
        }
        if (featureGroup.getFeatures().isEmpty()) {
            throw new PersistenceException("Features of feature group may not be empty");
        }

        featureGroup.getFeatures().forEach(feature -> {
            if (feature.getFeatureGroup() != featureGroup) {
                throw new PersistenceException("Feature may not belong to different feature group");
            }
        });
    }
}
