package dk.sunepoulsen.tes.features.service.domains.persistence;

import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface FeatureGroupRepository extends ListCrudRepository<FeatureGroupEntity, Long> {
    @Query("""
            SELECT g
            FROM FeatureGroupEntity g
            WHERE lower(g.key) = lower(:key)
        """)
    Optional<FeatureGroupEntity> findByKey(@Param("key") String key);
}
