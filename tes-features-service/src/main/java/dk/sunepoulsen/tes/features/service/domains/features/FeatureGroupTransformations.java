package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.FeatureGroup;
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupActivationEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class FeatureGroupTransformations {

    private final FeatureGroupActivationTransformations featureGroupActivationTransformations;
    private final FeatureTransformations featureTransformations;

    RegisterFeatureGroup toRegisterModel(FeatureGroupEntity featureGroupEntity) {
        RegisterFeatureGroup result = new RegisterFeatureGroup();

        result.setKey(featureGroupEntity.getKey());
        result.setName(featureGroupEntity.getName());
        result.setDescription(featureGroupEntity.getDescription());

        if (featureGroupEntity.getActivations() != null) {
            result.setActivations(featureGroupEntity.getActivations().stream()
                .map(featureGroupActivationTransformations::toModel)
                .toList()
            );
        }

        if (featureGroupEntity.getFeatures() != null) {
            result.setFeatures(featureGroupEntity.getFeatures().stream()
                .map(featureTransformations::toRegisterModel)
                .toList()
            );
        }

        return result;
    }

    FeatureGroup toFeatureGroupModel(FeatureGroupEntity featureGroupEntity) {
        FeatureGroup result = new FeatureGroup();

        result.setKey(featureGroupEntity.getKey());
        result.setName(featureGroupEntity.getName());
        result.setDescription(featureGroupEntity.getDescription());

        return result;
    }

    FeatureGroupEntity toEntity(RegisterFeatureGroup registerFeatureGroup) {
        FeatureGroupEntity featureGroupEntity = FeatureGroupEntity.builder()
            .key(registerFeatureGroup.getKey())
            .name(registerFeatureGroup.getName())
            .description(registerFeatureGroup.getDescription())
            .build();

        if (registerFeatureGroup.getActivations() != null) {
            featureGroupEntity.setActivations(registerFeatureGroup.getActivations().stream()
                .map(featureActivation -> {
                    FeatureGroupActivationEntity activationEntity = featureGroupActivationTransformations.toEntity(featureActivation);
                    activationEntity.setFeatureGroup(featureGroupEntity);

                    return activationEntity;
                })
                .toList()
            );
        }

        if (registerFeatureGroup.getFeatures() != null) {
            featureGroupEntity.setFeatures(registerFeatureGroup.getFeatures().stream()
                .map(feature -> {
                    FeatureEntity featureEntity = featureTransformations.toEntity(feature);
                    featureEntity.setFeatureGroup(featureGroupEntity);

                    return featureEntity;
                })
                .toList()
            );
        }

        return featureGroupEntity;
    }

}
