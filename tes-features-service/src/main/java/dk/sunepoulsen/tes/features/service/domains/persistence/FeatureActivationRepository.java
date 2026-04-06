package dk.sunepoulsen.tes.features.service.domains.persistence;

import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureActivationEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
    List<FeatureActivationEntity> findAllByFeatureId(@Param("featureId") Long featureId);
}
