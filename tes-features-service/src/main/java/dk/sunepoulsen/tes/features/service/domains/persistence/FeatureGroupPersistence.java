package dk.sunepoulsen.tes.features.service.domains.persistence;

import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupActivationEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity;
import dk.sunepoulsen.tes.springboot.rest.logic.PatchUtilities;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.PersistenceException;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Persistence service for feature groups.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureGroupPersistence {

    static final String FEATURE_GROUP_NOT_FOUND_MESSAGE = "No feature group with key '%s' exists";
    private static final String FEATURE_GROUP_ACTIVATION_NOT_FOUND_MESSAGE = "No activation with id '%s' and feature group '%s' exists";
    private static final String FEATURE_GROUP_KEY_PARAM = "feature_group_key";

    private final FeatureGroupRepository featureGroupRepository;
    private final FeaturePersistence featurePersistence;
    private final FeatureRepository featureRepository;

    private final FeatureGroupActivationRepository featureGroupActivationRepository;

    /**
     * Registers a feature group.
     *
     * @param featureGroup the feature group to register
     * @return the registered feature group
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
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

    /**
     * Returns all feature groups.
     *
     * @return a list of all feature groups
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
    public List<FeatureGroupEntity> getFeatureGroups() throws PersistenceException {
        return featureGroupRepository.findAll();
    }

    /**
     * Returns a feature group.
     *
     * @param featureGroupKey the feature group key
     * @return an {@link Optional} with the feature group
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
    public Optional<FeatureGroupEntity> getFeatureGroup(String featureGroupKey) throws PersistenceException {
        return featureGroupRepository.findByKey(featureGroupKey);
    }

    /**
     * Patches the information of a feature group.
     * <p>
     * Only these properties of a feature group can be patched:
     * <ul>
     *     <li>name</li>
     *     <li>description</li>
     * </ul>
     * All other properties in the passed {@code FeatureGroupEntity} will be ignored.
     *
     * @param key                The key of the feature group that will be patched.
     * @param featureGroupEntity New property values of the feature group to be patched.
     * @return The {@code FeatureGroupEntity} after it has been patched.
     * @throws PersistenceException In case of persistence errors.
     */
    @Transactional
    public Optional<FeatureGroupEntity> patchFeatureGroup(final String key, final FeatureGroupEntity featureGroupEntity) throws PersistenceException {
        final FeatureGroupEntity foundEntity = featureGroupRepository.findForUpdate(key).orElseThrow(() ->
            new ResourceNotFoundException(FEATURE_GROUP_KEY_PARAM, String.format(FEATURE_GROUP_NOT_FOUND_MESSAGE, key))
        );

        foundEntity.setName(PatchUtilities.patchValue(foundEntity.getName(), featureGroupEntity.getName()));
        foundEntity.setDescription(PatchUtilities.patchValue(foundEntity.getDescription(), featureGroupEntity.getDescription()));

        featureGroupRepository.save(foundEntity);

        return featureGroupRepository.findByKey(key);
    }

    /**
     * Deletes a feature group.
     *
     * @param featureGroupKey the feature group key
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
    public void deleteFeatureGroup(final String featureGroupKey) throws PersistenceException {
        final FeatureGroupEntity foundEntity = featureGroupRepository.findByKey(featureGroupKey).orElseThrow(() ->
            new ResourceNotFoundException(FEATURE_GROUP_KEY_PARAM, String.format(FEATURE_GROUP_NOT_FOUND_MESSAGE, featureGroupKey))
        );

        featureGroupRepository.delete(foundEntity);
    }

    /**
     * Creates a new activation for the given feature group.
     *
     * @param featureGroupKey  the feature group key
     * @param activationEntity the activation to create
     * @return the created activation
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
    public FeatureGroupActivationEntity createActivation(final String featureGroupKey, final FeatureGroupActivationEntity activationEntity) throws PersistenceException {
        final FeatureGroupEntity foundEntity = featureGroupRepository.findForUpdate(featureGroupKey).orElseThrow(() ->
            new ResourceNotFoundException(FEATURE_GROUP_KEY_PARAM, String.format(FEATURE_GROUP_NOT_FOUND_MESSAGE, featureGroupKey))
        );

        activationEntity.setFeatureGroup(foundEntity);
        return featureGroupActivationRepository.save(activationEntity);
    }

    /**
     * Returns a list of all activations for the given feature group.
     *
     * @param featureGroupKey the feature group key
     * @return the activations
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
    public List<FeatureGroupActivationEntity> getActivations(final String featureGroupKey) throws PersistenceException {
        final FeatureGroupEntity foundEntity = featureGroupRepository.findByKey(featureGroupKey).orElseThrow(() ->
            new ResourceNotFoundException(FEATURE_GROUP_KEY_PARAM, String.format(FEATURE_GROUP_NOT_FOUND_MESSAGE, featureGroupKey))
        );

        return featureGroupActivationRepository.findAllByFeatureGroup(foundEntity.getId());
    }

    /**
     * Returns a specific activation for the given feature group.
     *
     * @param featureGroupKey the feature group key
     * @param activationId    the activation id
     * @return the activation
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
    public Optional<FeatureGroupActivationEntity> getActivation(final String featureGroupKey, final Long activationId) throws PersistenceException {
        final FeatureGroupEntity foundEntity = featureGroupRepository.findByKey(featureGroupKey).orElseThrow(() ->
            new ResourceNotFoundException(FEATURE_GROUP_KEY_PARAM, String.format(FEATURE_GROUP_NOT_FOUND_MESSAGE, featureGroupKey))
        );

        return featureGroupActivationRepository.findActivation(foundEntity.getId(), activationId);
    }

    /**
     * Patches the information of an activation in a feature group.
     * <p>
     * All properties of an activation can be patchedm except {@code id} and {@code featureGroup}.
     *
     * @param featureGroupKey Key of the feature group that contains the activation to be patched.
     * @param activationId    Id of the activation that will be patched.
     * @param newValues       New property values of the activation to be patched.
     * @return The {@code FeatureGroupActivationEntity} after it has been patched.
     * @throws PersistenceException In case of persistence errors.
     */
    @Transactional
    public Optional<FeatureGroupActivationEntity> patchActivation(final String featureGroupKey, final Long activationId, final FeatureGroupActivationEntity newValues) throws PersistenceException {
        final FeatureGroupActivationEntity foundEntity = featureGroupActivationRepository.findActivationForUpdate(featureGroupKey, activationId).orElseThrow(() ->
            new ResourceNotFoundException(String.format(FEATURE_GROUP_ACTIVATION_NOT_FOUND_MESSAGE, activationId, featureGroupKey))
        );

        foundEntity.setEnabled(PatchUtilities.patchValue(foundEntity.getEnabled(), newValues.getEnabled()));
        foundEntity.setDateTime(PatchUtilities.patchValue(foundEntity.getDateTime(), newValues.getDateTime()));

        featureGroupActivationRepository.save(foundEntity);

        return featureGroupActivationRepository.findActivation(foundEntity.getFeatureGroup().getId(), activationId);
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
