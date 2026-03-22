package dk.sunepoulsen.tes.features.service.domains.features

import dk.sunepoulsen.tes.data.generators.DataGenerator
import dk.sunepoulsen.tes.data.generators.Generators
import dk.sunepoulsen.tes.data.generators.NumberGenerators
import dk.sunepoulsen.tes.features.model.Feature
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.features.model.RegisterFeature
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureActivationEntity
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class FeatureTransformationsSpec extends Specification {

    private FeatureTransformations sut
    private DataGenerator<String> textGenerator

    void setup() {
        this.sut = new FeatureTransformations(new FeatureActivationTransformations())
        this.textGenerator = Generators.textGenerator(NumberGenerators.integerGenerator(10, 50))
    }

    @Unroll
    void "Transform FeatureEntity to model with no associations: #_testcase"() {
        given:
            FeatureEntity featureEntity = FeatureEntity.builder()
                .id(1L)
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .activations(_activations)
                .build()

        expect:
            this.sut.toRegisterModel(featureEntity) == new RegisterFeature(
                key: featureEntity.key,
                name: featureEntity.name,
                description: featureEntity.description,
                activations: _modelActivations
            )

        where:
            _testcase           | _activations | _modelActivations
            'null activations'  | null         | null
            'empty activations' | []           | []
    }

    void "Transform FeatureEntity to model with 2 of activations"() {
        given:
            FeatureEntity featureEntity = FeatureEntity.builder()
                .id(1L)
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            featureEntity.activations = [
                new FeatureActivationEntity(
                    id: 1L,
                    feature: featureEntity,
                    enabled: true
                ),
                new FeatureActivationEntity(
                    id: 2L,
                    feature: featureEntity,
                    enabled: false,
                    dateTime: ZonedDateTime.now().plusWeeks(1)
                )
            ]

        expect:
            this.sut.toRegisterModel(featureEntity) == new RegisterFeature(
                key: featureEntity.key,
                name: featureEntity.name,
                description: featureEntity.description,
                activations: [
                    new FeatureActivation(
                        id: featureEntity.activations[0].id,
                        enabled: featureEntity.activations[0].enabled,
                        datetime: featureEntity.activations[0].dateTime
                    ),
                    new FeatureActivation(
                        id: featureEntity.activations[1].id,
                        enabled: featureEntity.activations[1].enabled,
                        datetime: featureEntity.activations[1].dateTime
                    )
                ])
    }

    void "Transform FeatureEntity to model of Feature"() {
        given:
            FeatureEntity featureEntity = FeatureEntity.builder()
                .id(1L)
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            featureEntity.setActivations(
                List.of(new FeatureActivationEntity(
                    feature: featureEntity,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                ))
            )

        expect:
            this.sut.toFeatureModel(featureEntity) == new Feature(
                key: featureEntity.key,
                name: featureEntity.name,
                description: featureEntity.description
            )
    }

    @Unroll
    void "Transform Feature to entity with no associations: #_testcase"() {
        given:
            RegisterFeature feature = new RegisterFeature(
                key: textGenerator.generate(),
                name: textGenerator.generate(),
                description: textGenerator.generate(),
                activations: _activations
            )

        expect:
            this.sut.toEntity(feature) == FeatureEntity.builder()
                .key(feature.key)
                .name(feature.name)
                .description(feature.description)
                .activations(_modelActivations)
                .build()

        where:
            _testcase           | _activations | _modelActivations
            'null activations'  | null         | null
            'empty activations' | []           | []
    }

    void "Transform Feature to entity with 2 of activations"() {
        given:
            RegisterFeature feature = new RegisterFeature(
                key: textGenerator.generate(),
                name: textGenerator.generate(),
                description: textGenerator.generate(),
                activations: [
                    new FeatureActivation(
                        id: 1L,
                        enabled: true,
                    ),
                    new FeatureActivation(
                        id: 2L,
                        enabled: true,
                        datetime: ZonedDateTime.now().plusWeeks(1)
                    )
                ]
            )

        when:
            FeatureEntity result = this.sut.toEntity(feature)

        then:
            result == FeatureEntity.builder()
                .key(feature.key)
                .name(feature.name)
                .description(feature.description)
                .build()

            result.activations == [
                new FeatureActivationEntity(
                    id: feature.activations[0].id,
                    feature: result,
                    enabled: feature.activations[0].enabled,
                    dateTime: feature.activations[0].datetime
                ),
                new FeatureActivationEntity(
                    id: feature.activations[1].id,
                    feature: result,
                    enabled: feature.activations[1].enabled,
                    dateTime: feature.activations[1].datetime
                ),
            ]
    }

}
