package dk.sunepoulsen.tes.features.service.domains.persistence;

import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureActivationEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity;
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
 * Persistence service for features.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeaturePersistence {

    private static final String FEATURE_NOT_FOUND_MESSAGE = "No feature with feature group '%s' and feature '%s' exists";
    private static final String FEATURE_GROUP_ACTIVATION_NOT_FOUND_MESSAGE = "No activation with id '%s' in feature group '%s' and feature '%s' exists";

    private final FeatureGroupRepository featureGroupRepository;
    private final FeatureRepository featureRepository;
    private final FeatureActivationRepository featureActivationRepository;

    /**
     * Registers a feature.
     *
     * @param feature the feature to register
     * @return the registered feature
     * @throws PersistenceException in case of persistence errors
     */
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

    /**
     * Returns all features for a feature group.
     *
     * @param featureGroupKey the feature group key
     * @return a list of all features
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
    public List<FeatureEntity> getFeatures(final String featureGroupKey) throws PersistenceException {
        final FeatureGroupEntity foundEntity = featureGroupRepository.findByKey(featureGroupKey).orElseThrow(() ->
            new ResourceNotFoundException("feature_group_key", String.format(FeatureGroupPersistence.FEATURE_GROUP_NOT_FOUND_MESSAGE, featureGroupKey))
        );

        return featureRepository.findByFeatureGroup(foundEntity);
    }

    /**
     * Returns a feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @return an {@link Optional} with the feature
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
    public Optional<FeatureEntity> getFeature(final String featureGroupKey, final String featureKey) throws PersistenceException {
        return featureRepository.findByKey(featureGroupKey, featureKey);
    }

    /**
     * Patches the information of a feature.
     * <p>
     * Only these properties of a feature can be patched:
     * <ul>
     *     <li>name</li>
     *     <li>description</li>
     * </ul>
     * All other properties in the passed {@code FeatureEntity} will be ignored.
     *
     * @param featureGroupKey The key of the feature group that contains the feature to be patched.
     * @param featureKey      The key of the feature that will be patched.
     * @param featureEntity   New property values of the feature to be patched.
     * @return The {@code FeatureEntity} after it has been patched.
     * @throws PersistenceException In case of persistence errors.
     */
    @Transactional
    public Optional<FeatureEntity> patchFeature(final String featureGroupKey, final String featureKey, final FeatureEntity featureEntity) throws PersistenceException {
        final FeatureEntity foundEntity = featureRepository.findForUpdate(featureGroupKey, featureKey).orElseThrow(() ->
            new ResourceNotFoundException(String.format(FEATURE_NOT_FOUND_MESSAGE, featureGroupKey, featureKey))
        );

        foundEntity.setName(PatchUtilities.patchValue(foundEntity.getName(), featureEntity.getName()));
        foundEntity.setDescription(PatchUtilities.patchValue(foundEntity.getDescription(), featureEntity.getDescription()));

        featureRepository.save(foundEntity);

        return featureRepository.findByKey(featureGroupKey, featureKey);
    }

    /**
     * Deletes a feature identified by feature group key and feature key.
     *
     * @param featureGroupKey The key of the feature group that contains the feature to be deleted.
     * @param featureKey      The key of the feature that will be deleted.
     * @throws PersistenceException In case of persistence errors.
     */
    @Transactional
    public void deleteFeature(final String featureGroupKey, final String featureKey) throws PersistenceException {
        final FeatureEntity foundEntity = featureRepository.findByKey(featureGroupKey, featureKey).orElseThrow(() ->
            new ResourceNotFoundException(String.format(FEATURE_NOT_FOUND_MESSAGE, featureGroupKey, featureKey))
        );

        featureRepository.delete(foundEntity);
    }

    /**
     * Returns a list of all activations for the given feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey the feature key
     * @return the activations
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
    public List<FeatureActivationEntity> getActivations(final String featureGroupKey, final String featureKey) throws PersistenceException {
        final FeatureEntity foundEntity = featureRepository.findByKey(featureGroupKey, featureKey).orElseThrow(() ->
            new ResourceNotFoundException(String.format(FEATURE_NOT_FOUND_MESSAGE, featureGroupKey, featureKey))
        );

        return featureActivationRepository.findAllByFeature(foundEntity.getId());
    }

    /**
     * Creates a new activation for the given feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey the feature key
     * @param activationEntity the activation to create
     * @return the created activation
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
    public FeatureActivationEntity createActivation(final String featureGroupKey, final String featureKey, final FeatureActivationEntity activationEntity) throws PersistenceException {
        final FeatureEntity foundEntity = featureRepository.findForUpdate(featureGroupKey, featureKey).orElseThrow(() ->
            new ResourceNotFoundException(String.format(FEATURE_NOT_FOUND_MESSAGE, featureGroupKey, featureKey))
        );

        activationEntity.setFeature(foundEntity);
        return featureActivationRepository.save(activationEntity);
    }

    /**
     * Returns a specific activation for the given feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @param activationId    the activation id
     * @return the activation
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
    public Optional<FeatureActivationEntity> getActivation(final String featureGroupKey, final String featureKey, final Long activationId) throws PersistenceException {
        final FeatureEntity foundEntity = featureRepository.findByKey(featureGroupKey, featureKey).orElseThrow(() ->
            new ResourceNotFoundException(String.format(FEATURE_NOT_FOUND_MESSAGE, featureGroupKey, featureKey))
        );

        return featureActivationRepository.findActivation(foundEntity.getId(), activationId);
    }

    /**
     * Patches a specific activation for the given feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @param activationId    the activation id
     * @return the activation after it has been patched.
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
    public Optional<FeatureActivationEntity> patchActivation(final String featureGroupKey, final String featureKey, final Long activationId, final FeatureActivationEntity newActivation) throws PersistenceException {
        final FeatureActivationEntity foundEntity = featureActivationRepository.findActivationForUpdate(featureGroupKey, featureKey, activationId).orElseThrow(() ->
            new ResourceNotFoundException(String.format(FEATURE_GROUP_ACTIVATION_NOT_FOUND_MESSAGE, activationId, featureGroupKey, featureKey))
        );

        foundEntity.setEnabled(PatchUtilities.patchValue(foundEntity.getEnabled(), newActivation.getEnabled()));
        foundEntity.setDateTime(PatchUtilities.patchValue(foundEntity.getDateTime(), newActivation.getDateTime()));

        return featureActivationRepository.findById(foundEntity.getId());
    }

    /**
     * Delete a specific activation for the given feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @param activationId    the activation id
     * @throws PersistenceException in case of persistence errors
     */
    @Transactional
    public void deleteActivation(final String featureGroupKey, final String featureKey, final Long activationId) throws PersistenceException {
        final FeatureActivationEntity foundEntity = featureActivationRepository.findActivationForUpdate(featureGroupKey, featureKey, activationId).orElseThrow(() ->
            new ResourceNotFoundException(String.format(FEATURE_GROUP_ACTIVATION_NOT_FOUND_MESSAGE, activationId, featureGroupKey, featureKey))
        );

        if (foundEntity.getFeature().getActivations() != null) {
            foundEntity.getFeature().getActivations().removeIf(activation -> activationId.equals(activation.getId()));
        }

        featureActivationRepository.deleteById(foundEntity.getId());
    }

}
