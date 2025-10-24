package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.Feature;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureActivationEntity;
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class FeatureTransformations {

    private final FeatureActivationTransformations featureActivationTransformations;

    Feature toModel(FeatureEntity featureEntity) {
        Feature result = new Feature();

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

    FeatureEntity toEntity(Feature feature) {
        FeatureEntity featureEntity = FeatureEntity.builder()
            .key(feature.getKey())
            .name(feature.getName())
            .description(feature.getDescription())
            .build();

        if (feature.getActivations() != null) {
            featureEntity.setActivations(feature.getActivations().stream()
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
