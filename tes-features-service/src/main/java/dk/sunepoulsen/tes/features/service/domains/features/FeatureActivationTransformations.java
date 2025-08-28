package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureActivationEntity;
import org.springframework.stereotype.Service;

@Service
class FeatureActivationTransformations extends ActivationTransformations {

    FeatureActivation toModel(FeatureActivationEntity entity) {
        return super.toModelBuilder(entity)
            .id(entity.getId())
            .build();
    }

    FeatureActivationEntity toEntity(FeatureActivation featureActivation) {
        FeatureActivationEntity entity = new FeatureActivationEntity();

        super.assignActivationEntity(entity, featureActivation);
        entity.setId(featureActivation.getId());

        return entity;
    }

}
