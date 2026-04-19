package dk.sunepoulsen.tes.features.service.domains.persistence;

import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupActivationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for feature group activations.
 */
interface FeatureGroupActivationRepository extends ListCrudRepository<FeatureGroupActivationEntity, Long> {
    /**
     * Returns all activations for the given feature group.
     *
     * @param featureGroupId the feature group id
     * @return a list of all found activations
     */
    @Query("""
            SELECT a
            FROM FeatureGroupActivationEntity a
            WHERE a.featureGroup.id = :featureGroupId
        """)
    List<FeatureGroupActivationEntity> findAllByFeatureGroup(@Param("featureGroupId") Long featureGroupId);

    /**
     * Returns a specific activation for the given feature group.
     *
     * @param id             the activation id
     * @param featureGroupId the feature group id
     * @return the activation if found
     */
    @Query("""
            SELECT a
            FROM FeatureGroupActivationEntity a
            WHERE a.featureGroup.id = :featureGroupId AND a.id = :id
        """)
    Optional<FeatureGroupActivationEntity> findActivation(@Param("featureGroupId") Long featureGroupId, @Param("id") Long id);

    /**
     * Returns the activation for the given feature group and locks it for update.
     *
     * @param featureGroupKey the feature group key
     * @param id              the activation id
     * @return the activation of the feature group if found
     */
    @Query("""
            SELECT a
            FROM FeatureGroupActivationEntity a
            WHERE lower(a.featureGroup.key) = lower(:featureGroupKey) AND a.id = :id
        """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FeatureGroupActivationEntity> findActivationForUpdate(@Param("featureGroupKey") String featureGroupKey, @Param("id") Long id);
}
