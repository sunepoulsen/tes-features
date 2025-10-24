package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupActivationEntity;
import org.springframework.stereotype.Service;

@Service
class FeatureGroupActivationTransformations extends ActivationTransformations {

    FeatureActivation toModel(FeatureGroupActivationEntity entity) {
        FeatureActivation result = super.toModel(entity);
        result.setId(entity.getId());

        return result;
    }

    FeatureGroupActivationEntity toEntity(FeatureActivation featureActivation) {
        FeatureGroupActivationEntity entity = new FeatureGroupActivationEntity();

        super.assignActivationEntity(entity, featureActivation);
        entity.setId(featureActivation.getId());

        return entity;
    }

}
