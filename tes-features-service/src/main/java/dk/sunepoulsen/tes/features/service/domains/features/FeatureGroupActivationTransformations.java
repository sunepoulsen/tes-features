package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupActivationEntity;
import org.springframework.stereotype.Service;

@Service
class FeatureGroupActivationTransformations extends ActivationTransformations {

    FeatureActivation toModel(FeatureGroupActivationEntity entity) {
        return super.toModelBuilder(entity)
            .id(entity.getId())
            .build();
    }

    FeatureGroupActivationEntity toEntity(FeatureActivation featureActivation) {
        FeatureGroupActivationEntity entity = new FeatureGroupActivationEntity();

        super.assignActivationEntity(entity, featureActivation);
        entity.setId(featureActivation.getId());

        return entity;
    }

}
