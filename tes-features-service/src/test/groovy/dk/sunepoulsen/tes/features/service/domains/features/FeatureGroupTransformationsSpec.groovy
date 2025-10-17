package dk.sunepoulsen.tes.features.service.domains.features

import dk.sunepoulsen.tes.data.generators.DataGenerator
import dk.sunepoulsen.tes.data.generators.Generators
import dk.sunepoulsen.tes.data.generators.NumberGenerators
import dk.sunepoulsen.tes.features.model.Feature
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.features.model.FeatureGroup
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureActivationEntity
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupActivationEntity
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity
import spock.lang.Specification
import spock.lang.Unroll

import java.time.ZonedDateTime

class FeatureGroupTransformationsSpec extends Specification {

    private FeatureGroupTransformations sut
    private DataGenerator<String> textGenerator

    void setup() {
        this.sut = new FeatureGroupTransformations(
            new FeatureGroupActivationTransformations(),
            new FeatureTransformations(
                new FeatureActivationTransformations()
            )
        )
        this.textGenerator = Generators.textGenerator(NumberGenerators.integerGenerator(10, 50))
    }

    @Unroll
    void "Transform FeatureGroupEntity to model with no associations: #_testcase"() {
        given:
            FeatureGroupEntity featureGroupEntity = new FeatureGroupEntity(
                id: 1L,
                key: textGenerator.generate(),
                name: textGenerator.generate(),
                description: textGenerator.generate(),
                features: _features,
                activations: _activations
            )

        expect:
            this.sut.toModel(featureGroupEntity) == new FeatureGroup(
                key: featureGroupEntity.key,
                name: featureGroupEntity.name,
                description: featureGroupEntity.description,
                features: _modelFeatures,
                activations: _modelActivations
            )

        where:
            _testcase                            | _features | _modelFeatures | _activations | _modelActivations
            'null features & null activations'   | null      | null           | null         | null
            'null features & empty activations'  | null      | null           | []           | []
            'empty features & null activations'  | []        | []             | null         | null
            'empty features & empty activations' | []        | []             | []           | []
    }

    void "Transform FeatureGroupEntity to model with associations"() {
        given:
            FeatureGroupEntity featureGroupEntity = FeatureGroupEntity.builder()
                .id(1L)
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            featureGroupEntity.features = [
                FeatureEntity.builder()
                    .id(1L)
                    .featureGroup(featureGroupEntity)
                    .key(textGenerator.generate())
                    .name(textGenerator.generate())
                    .description(textGenerator.generate())
                    .activations([])
                    .build()
            ]

            featureGroupEntity.activations = [
                new FeatureGroupActivationEntity(
                    id: 1L,
                    featureGroup: featureGroupEntity,
                    enabled: true
                ),
                new FeatureGroupActivationEntity(
                    id: 2L,
                    featureGroup: featureGroupEntity,
                    enabled: false,
                    dateTime: ZonedDateTime.now().plusWeeks(1)
                )
            ]

        expect:
            this.sut.toModel(featureGroupEntity) == new FeatureGroup(
                key: featureGroupEntity.key,
                name: featureGroupEntity.name,
                description: featureGroupEntity.description,
                features: [
                    new Feature(
                        key: featureGroupEntity.features[0].key,
                        name: featureGroupEntity.features[0].name,
                        description: featureGroupEntity.features[0].description,
                        activations: []
                    )
                ],
                activations: [
                    new FeatureActivation(
                        id: featureGroupEntity.activations[0].id,
                        enabled: featureGroupEntity.activations[0].enabled,
                        datetime: featureGroupEntity.activations[0].dateTime
                    ),
                    new FeatureActivation(
                        id: featureGroupEntity.activations[1].id,
                        enabled: featureGroupEntity.activations[1].enabled,
                        datetime: featureGroupEntity.activations[1].dateTime
                    )

                ]
            )

    }

    @Unroll
    void "Transform FeatureGroup to entity with no associations: #_testcase"() {
        given:
            FeatureGroup featureGroup = new FeatureGroup(
                key: textGenerator.generate(),
                name: textGenerator.generate(),
                description: textGenerator.generate(),
                features: _features,
                activations: _activations
            )

        expect:
            this.sut.toEntity(featureGroup) == FeatureGroupEntity.builder()
                .key(featureGroup.key)
                .name(featureGroup.name)
                .description(featureGroup.description)
                .features(_modelFeatures)
                .activations(_modelActivations)
                .build()

        where:
            _testcase                            | _features | _modelFeatures | _activations | _modelActivations
            'null features & null activations'   | null      | null           | null         | null
            'null features & empty activations'  | null      | null           | []           | []
            'empty features & null activations'  | []        | []             | null         | null
            'empty features & empty activations' | []        | []             | []           | []
    }

    void "Transform FeatureGroup to entity with 2 features and 2 activations"() {
        given:
            FeatureGroup featureGroup = new FeatureGroup(
                key: textGenerator.generate(),
                name: textGenerator.generate(),
                description: textGenerator.generate(),
                features: [
                    new Feature(
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
                    ),
                    new Feature(
                        key: textGenerator.generate(),
                        name: textGenerator.generate(),
                        description: textGenerator.generate(),
                        activations: [
                            new FeatureActivation(
                                id: 3L,
                                enabled: true,
                            ),
                            new FeatureActivation(
                                id: 4L,
                                enabled: true,
                                datetime: ZonedDateTime.now().plusWeeks(1)
                            )
                        ]
                    )
                ],
                activations: [
                    new FeatureActivation(
                        id: 5L,
                        enabled: true,
                    ),
                    new FeatureActivation(
                        id: 6L,
                        enabled: true,
                        datetime: ZonedDateTime.now().plusWeeks(1)
                    )
                ]
            )

        when:
            FeatureGroupEntity result = this.sut.toEntity(featureGroup)

        then:
            result == FeatureGroupEntity.builder()
                .key(featureGroup.key)
                .name(featureGroup.name)
                .description(featureGroup.description)
                .build()

            ArrayList<FeatureEntity> expectedFeatureEntities = [
                FeatureEntity.builder()
                    .featureGroup(result)
                    .key(featureGroup.features[0].key)
                    .name(featureGroup.features[0].name)
                    .description(featureGroup.features[0].description)
                    .build(),
                FeatureEntity.builder()
                    .featureGroup(result)
                    .key(featureGroup.features[1].key)
                    .name(featureGroup.features[1].name)
                    .description(featureGroup.features[1].description)
                    .build()
            ]

            result.features == expectedFeatureEntities
            result.features[0].activations == [
                new FeatureActivationEntity(
                    id: featureGroup.features[0].activations[0].id,
                    feature: expectedFeatureEntities[0],
                    enabled: featureGroup.features[0].activations[0].enabled,
                    dateTime: featureGroup.features[0].activations[0].datetime
                ),
                new FeatureActivationEntity(
                    id: featureGroup.features[0].activations[1].id,
                    feature: expectedFeatureEntities[0],
                    enabled: featureGroup.features[0].activations[1].enabled,
                    dateTime: featureGroup.features[0].activations[1].datetime
                ),
            ]
            result.features[1].activations == [
                new FeatureActivationEntity(
                    id: featureGroup.features[1].activations[0].id,
                    feature: expectedFeatureEntities[1],
                    enabled: featureGroup.features[1].activations[0].enabled,
                    dateTime: featureGroup.features[1].activations[0].datetime
                ),
                new FeatureActivationEntity(
                    id: featureGroup.features[1].activations[1].id,
                    feature: expectedFeatureEntities[1],
                    enabled: featureGroup.features[1].activations[1].enabled,
                    dateTime: featureGroup.features[1].activations[1].datetime
                ),
            ]

            result.activations == [
                new FeatureGroupActivationEntity(
                    id: featureGroup.activations[0].id,
                    featureGroup: result,
                    enabled: featureGroup.activations[0].enabled,
                    dateTime: featureGroup.activations[0].datetime
                ),
                new FeatureGroupActivationEntity(
                    id: featureGroup.activations[1].id,
                    featureGroup: result,
                    enabled: featureGroup.activations[1].enabled,
                    dateTime: featureGroup.activations[1].datetime
                )
            ]
    }
}