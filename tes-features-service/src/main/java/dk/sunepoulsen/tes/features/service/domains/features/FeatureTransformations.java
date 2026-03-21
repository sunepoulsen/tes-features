package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.RegisterFeature;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureActivationEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class FeatureTransformations {

    private final FeatureActivationTransformations featureActivationTransformations;

    RegisterFeature toRegisterModel(FeatureEntity featureEntity) {
        RegisterFeature result = new RegisterFeature();

        result.setKey(featureEntity.getKey());
        result.setName(featureEntity.getName());
        result.setDescription(featureEntity.getDescription());

        if (featureEntity.getActivations() != null) {
            result.setActivations(featureEntity.getActivations().stream()
                .map(featureActivationTransformations::toModel)
                .toList()
            );
        }

        return result;
    }

    FeatureEntity toEntity(RegisterFeature registerFeature) {
        FeatureEntity featureEntity = FeatureEntity.builder()
            .key(registerFeature.getKey())
            .name(registerFeature.getName())
            .description(registerFeature.getDescription())
            .build();

        if (registerFeature.getActivations() != null) {
            featureEntity.setActivations(registerFeature.getActivations().stream()
                .map(featureActivation -> {
                    FeatureActivationEntity activationEntity = featureActivationTransformations.toEntity(featureActivation);
                    activationEntity.setFeature(featureEntity);

                    return activationEntity;
                })
                .toList()
            );
        }

        return featureEntity;
    }

}
