package dk.sunepoulsen.tes.features.service.domains.persistence;

import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupActivationEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface FeatureGroupActivationRepository extends ListCrudRepository<FeatureGroupActivationEntity, Long> {
}
