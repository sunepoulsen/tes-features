package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.FeatureGroup;
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

    FeatureGroup toModel(FeatureGroupEntity featureGroupEntity) {
        FeatureGroup.FeatureGroupBuilder builder = FeatureGroup.builder()
            .key(featureGroupEntity.getKey())
            .name(featureGroupEntity.getName())
            .description(featureGroupEntity.getDescription());

        if (featureGroupEntity.getActivations() != null) {
            builder.activations(featureGroupEntity.getActivations().stream()
                .map(featureGroupActivationTransformations::toModel)
                .toList()
            );
        }

        if (featureGroupEntity.getFeatures() != null) {
            builder.features(featureGroupEntity.getFeatures().stream()
                .map(featureTransformations::toModel)
                .toList()
            );
        }

        return builder.build();
    }

    FeatureGroupEntity toEntity(FeatureGroup featureGroup) {
        FeatureGroupEntity featureGroupEntity = FeatureGroupEntity.builder()
            .key(featureGroup.getKey())
            .name(featureGroup.getName())
            .description(featureGroup.getDescription())
            .build();

        if (featureGroup.getActivations() != null) {
            featureGroupEntity.setActivations(featureGroup.getActivations().stream()
                .map(featureActivation -> {
                    FeatureGroupActivationEntity activationEntity = featureGroupActivationTransformations.toEntity(featureActivation);
                    activationEntity.setFeatureGroup(featureGroupEntity);

                    return activationEntity;
                })
                .toList()
            );
        }

        if (featureGroup.getFeatures() != null) {
            featureGroupEntity.setFeatures(featureGroup.getFeatures().stream()
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
