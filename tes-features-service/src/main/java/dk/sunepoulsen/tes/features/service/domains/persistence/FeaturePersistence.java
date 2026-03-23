package dk.sunepoulsen.tes.features.service.domains.persistence;

import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.PersistenceException;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeaturePersistence {

    private final FeatureGroupRepository featureGroupRepository;
    private final FeatureRepository featureRepository;

    public FeatureEntity registerFeature(FeatureEntity feature) throws PersistenceException {
        if (feature.getFeatureGroup() == null) {
            throw new IllegalArgumentException("Feature group must not be null");
        }

        if (feature.getFeatureGroup().getId() == null) {
            throw new IllegalArgumentException("Feature group id must not be null");
        }

        if (!featureGroupRepository.existsById(feature.getFeatureGroup().getId())) {
            throw new ResourceNotFoundException("featureGroup", String.format("Feature group %s does not exist", feature.getFeatureGroup().getId()));
        }

        return featureRepository.findByKey(feature.getKey()).orElseGet(() -> {
            log.info("Creating new feature in feature group: {} -> {}", feature.getFeatureGroup().getKey(), feature.getKey());
            return featureRepository.save(feature);
        });
    }

    @Transactional
    public List<FeatureEntity> getFeatures(final String featureGroupKey) throws PersistenceException {
        final FeatureGroupEntity foundEntity = featureGroupRepository.findByKey(featureGroupKey).orElseThrow(() ->
            new ResourceNotFoundException("feature_group_key", "No feature group with key '" + featureGroupKey + "' exists")
        );

        return featureRepository.findByFeatureGroup(foundEntity);
    }

    @Transactional
    public Optional<FeatureEntity> getFeature(final String featureGroupKey, final String featureKey) throws PersistenceException {
        return featureRepository.findByKey(featureGroupKey, featureKey);
    }

}
