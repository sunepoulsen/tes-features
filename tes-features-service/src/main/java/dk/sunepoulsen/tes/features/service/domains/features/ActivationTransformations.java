package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.ActivationEntity;

class ActivationTransformations {

    protected FeatureActivation.FeatureActivationBuilder toModelBuilder(ActivationEntity activationEntity) {
        return FeatureActivation.builder()
            .enabled(activationEntity.getEnabled())
            .datetime(activationEntity.getDateTime());
    }

    protected void assignActivationEntity(ActivationEntity activationEntity, FeatureActivation featureActivation) {
        activationEntity.setEnabled(featureActivation.getEnabled());
        activationEntity.setDateTime(featureActivation.getDatetime());
    }

}
