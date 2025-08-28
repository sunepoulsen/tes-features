package dk.sunepoulsen.tes.features.service.domains.persistence;

import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureActivationEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface FeatureActivationRepository extends ListCrudRepository<FeatureActivationEntity, Long> {
    @Query("""
            SELECT a
            FROM FeatureActivationEntity a
            WHERE a.feature.id = :featureId
        """)
    List<FeatureActivationEntity> findAllByFeatureId(@Param("featureId") Long featureId);
}
