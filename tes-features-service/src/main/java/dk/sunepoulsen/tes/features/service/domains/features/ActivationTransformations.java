package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.ActivationEntity;

class ActivationTransformations {

    protected FeatureActivation toModel(ActivationEntity activationEntity) {
        FeatureActivation result = new FeatureActivation();
        result.setEnabled(activationEntity.getEnabled());
        result.setDatetime(activationEntity.getDateTime());

        return result;
    }

    protected void assignActivationEntity(ActivationEntity activationEntity, FeatureActivation featureActivation) {
        activationEntity.setEnabled(featureActivation.getEnabled());
        activationEntity.setDateTime(featureActivation.getDatetime());
    }

}
