package dk.sunepoulsen.tes.features.service.domains.persistence;

import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface FeatureRepository extends PagingAndSortingRepository<FeatureEntity, Long>, ListCrudRepository<FeatureEntity, Long> {
    @Query("""
            SELECT
            CASE WHEN count(f) > 0
                THEN TRUE
                ELSE FALSE
            END
            FROM FeatureEntity f
            WHERE lower(f.key) = lower(:key)
        """)
    boolean existsByKey(@Param("key") String key);

    @Query("""
            SELECT f
            FROM FeatureEntity f
            WHERE lower(f.key) = lower(:key)
        """)
    Optional<FeatureEntity> findByKey(@Param("key") String key);

    @Query("""
            SELECT f
            FROM FeatureEntity f
            WHERE f.featureGroup = :featureGroup
        """)
    List<FeatureEntity> findByFeatureGroup(@Param("featureGroup") FeatureGroupEntity featureGroup);
}
