package dk.sunepoulsen.tes.features.service.domains.persistence;

import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupActivationEntity;
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
    List<FeatureGroupActivationEntity> findAllByFeatureGroupId(@Param("featureGroupId") Long featureGroupId);

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
            WHERE a.id = :id AND a.featureGroup.id = :featureGroupId
        """)
    Optional<FeatureGroupActivationEntity> findByIdAndFeatureGroupId(@Param("id") Long id, @Param("featureGroupId") Long featureGroupId);
}
