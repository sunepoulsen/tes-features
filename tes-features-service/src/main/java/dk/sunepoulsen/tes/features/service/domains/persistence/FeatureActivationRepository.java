package dk.sunepoulsen.tes.features.service.domains.persistence;

import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureActivationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for feature activations.
 */
interface FeatureActivationRepository extends ListCrudRepository<FeatureActivationEntity, Long> {
    /**
     * Returns all activations for the given feature.
     *
     * @param featureId the feature id
     * @return a list of all found activations
     */
    @Query("""
            SELECT a
            FROM FeatureActivationEntity a
            WHERE a.feature.id = :featureId
        """)
    List<FeatureActivationEntity> findAllByFeature(@Param("featureId") Long featureId);

    /**
     * Returns a specific activation for the given feature.
     *
     * @param id        the activation id
     * @param featureId the feature id
     * @return the activation if found
     */
    @Query("""
            SELECT a
            FROM FeatureActivationEntity a
            WHERE a.feature.id = :featureId AND a.id = :id 
        """)
    Optional<FeatureActivationEntity> findActivation(@Param("featureId") Long featureId, @Param("id") Long id);

    /**
     * Returns a specific activation for the given feature that is locked for an update.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @param id              the activation id
     * @return the activation if found
     */
    @Query("""
            SELECT a
            FROM FeatureActivationEntity a
            WHERE lower(a.feature.featureGroup.key) = lower(:featureGroupKey) AND lower(a.feature.key) = lower(:featureKey) AND a.id = :id
        """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FeatureActivationEntity> findActivationForUpdate(@Param("featureGroupKey") String featureGroupKey, @Param("featureKey") String featureKey, @Param("id") Long id);
}
