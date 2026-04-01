package dk.sunepoulsen.tes.features.service.domains.persistence

import dk.sunepoulsen.tes.data.generators.DataGenerator
import dk.sunepoulsen.tes.data.generators.Generators
import dk.sunepoulsen.tes.data.generators.NumberGenerators
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupActivationEntity
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.PersistenceException
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import([FeatureGroupPersistenceTestService])
@ActiveProfiles(['ut'])
@Transactional
class FeatureGroupPersistenceSpec extends Specification {

    @Autowired
    private FeatureGroupRepository featureGroupRepository

    @Autowired
    private FeatureRepository featureRepository

    @Autowired
    private FeatureGroupPersistenceTestService featureGroupPersistenceTestService

    @Autowired
    private FeatureGroupPersistence sut

    private DataGenerator<String> textGenerator

    void setup() {
        this.textGenerator = Generators.textGenerator(NumberGenerators.integerGenerator(10, 50))
        this.featureGroupPersistenceTestService.deleteAll()
    }

    @Unroll
    void "Tests register feature group with wrong features: #_testcase"() {
        given:
            FeatureGroupEntity featureGroup = FeatureGroupEntity.builder()
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .features(_features)
                .build()

            featureGroup.setActivations([
                new FeatureGroupActivationEntity(
                    featureGroup: featureGroup,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])

        when:
            this.sut.registerFeatureGroup(featureGroup)

        then:
            PersistenceException ex = thrown(PersistenceException)
            ex.message == _message

        where:
            _testcase                                     | _features | _message
            'Features is null'                            | null      | 'Features of feature group may not be null'
            'Features is empty'                           | []        | 'Features of feature group may not be empty'
            'Features belongs to different feature group' | [new FeatureEntity(
                featureGroup: new FeatureGroupEntity()
            )]                                                        | 'Feature may not belong to different feature group'
    }

    void "Tests register feature group that does not exist"() {
        given:
            FeatureGroupEntity featureGroup = FeatureGroupEntity.builder()
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            featureGroup.setActivations([
                new FeatureGroupActivationEntity(
                    featureGroup: featureGroup,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])

            featureGroup.features = [FeatureEntity.builder()
                                         .featureGroup(featureGroup)
                                         .key(textGenerator.generate())
                                         .name(textGenerator.generate())
                                         .description(textGenerator.generate())
                                         .build()
            ]

        when:
            FeatureGroupEntity createdFeatureGroup = this.sut.registerFeatureGroup(featureGroup)
            this.featureGroupPersistenceTestService.flushDatabase()

        then:
            createdFeatureGroup.id > 0
            createdFeatureGroup.key == featureGroup.key
            createdFeatureGroup.name == featureGroup.name
            createdFeatureGroup.description == featureGroup.description

            createdFeatureGroup.activations.size() == 1
            createdFeatureGroup.activations.first.enabled == featureGroup.activations.first.enabled
            createdFeatureGroup.activations.first.dateTime == featureGroup.activations.first.dateTime

            createdFeatureGroup.features.size() == 1
            createdFeatureGroup.features.first.key == featureGroup.features.first.key
    }

    void "Tests register feature group that does exist with same feature"() {
        given:
            FeatureGroupEntity existingFeatureGroup = FeatureGroupEntity.builder()
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            existingFeatureGroup.setActivations([
                new FeatureGroupActivationEntity(
                    featureGroup: existingFeatureGroup,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])
            existingFeatureGroup.features = [FeatureEntity.builder()
                                                 .featureGroup(existingFeatureGroup)
                                                 .key(textGenerator.generate())
                                                 .name(textGenerator.generate())
                                                 .description(textGenerator.generate())
                                                 .build()
            ]

            this.sut.registerFeatureGroup(existingFeatureGroup)

        and:
            FeatureGroupEntity featureGroup = FeatureGroupEntity.builder()
                .key(existingFeatureGroup.key)
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            featureGroup.setActivations([
                new FeatureGroupActivationEntity(
                    featureGroup: existingFeatureGroup,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2023, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])
            featureGroup.features = [FeatureEntity.builder()
                                         .featureGroup(existingFeatureGroup)
                                         .key(existingFeatureGroup.features.first.key)
                                         .name(textGenerator.generate())
                                         .description(textGenerator.generate())
                                         .build()
            ]


        when:
            FeatureGroupEntity createdFeatureGroup = this.sut.registerFeatureGroup(existingFeatureGroup)
            this.featureGroupPersistenceTestService.flushDatabase()

        then:
            createdFeatureGroup.id == existingFeatureGroup.id
            createdFeatureGroup.key == existingFeatureGroup.key
            createdFeatureGroup.name == existingFeatureGroup.name
            createdFeatureGroup.description == existingFeatureGroup.description

            createdFeatureGroup.activations.size() == 1
            createdFeatureGroup.activations.first.enabled == existingFeatureGroup.activations.first.enabled
            createdFeatureGroup.activations.first.dateTime == existingFeatureGroup.activations.first.dateTime

            createdFeatureGroup.features.size() == 1
            createdFeatureGroup.features.first.key == existingFeatureGroup.features.first.key
            createdFeatureGroup.features.first.name == existingFeatureGroup.features.first.name
            createdFeatureGroup.features.first.description == existingFeatureGroup.features.first.description
    }

    void "Tests register feature group that does exist with extra new feature"() {
        given:
            FeatureGroupEntity existingFeatureGroup = FeatureGroupEntity.builder()
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            existingFeatureGroup.setActivations([
                new FeatureGroupActivationEntity(
                    featureGroup: existingFeatureGroup,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])
            existingFeatureGroup.features = [FeatureEntity.builder()
                                                 .featureGroup(existingFeatureGroup)
                                                 .key(textGenerator.generate())
                                                 .name(textGenerator.generate())
                                                 .description(textGenerator.generate())
                                                 .build()
            ]

            this.sut.registerFeatureGroup(existingFeatureGroup)

        and:
            FeatureGroupEntity featureGroup = FeatureGroupEntity.builder()
                .key(existingFeatureGroup.key)
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            featureGroup.setActivations([
                new FeatureGroupActivationEntity(
                    featureGroup: existingFeatureGroup,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2023, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])
            featureGroup.features = [FeatureEntity.builder()
                                         .featureGroup(featureGroup)
                                         .key(existingFeatureGroup.features.first.key)
                                         .name(textGenerator.generate())
                                         .description(textGenerator.generate())
                                         .build(),
                                     FeatureEntity.builder()
                                         .featureGroup(featureGroup)
                                         .key(existingFeatureGroup.features.first.key + ':1')
                                         .name(textGenerator.generate())
                                         .description(textGenerator.generate())
                                         .build()
            ]


        when:
            FeatureGroupEntity createdFeatureGroup = this.sut.registerFeatureGroup(featureGroup)
            this.featureGroupPersistenceTestService.flushDatabase()

        then:
            createdFeatureGroup.id == existingFeatureGroup.id
            createdFeatureGroup.key == existingFeatureGroup.key
            createdFeatureGroup.name == existingFeatureGroup.name
            createdFeatureGroup.description == existingFeatureGroup.description

            createdFeatureGroup.activations.size() == 1
            createdFeatureGroup.activations.first.enabled == existingFeatureGroup.activations.first.enabled
            createdFeatureGroup.activations.first.dateTime == existingFeatureGroup.activations.first.dateTime

            createdFeatureGroup.features.size() == 2
            createdFeatureGroup.features[0].key == existingFeatureGroup.features[0].key
            createdFeatureGroup.features[0].name == existingFeatureGroup.features[0].name
            createdFeatureGroup.features[0].description == existingFeatureGroup.features[0].description

            createdFeatureGroup.features[1].key == featureGroup.features[1].key
            createdFeatureGroup.features[1].name == featureGroup.features[1].name
            createdFeatureGroup.features[1].description == featureGroup.features[1].description
    }

    void "Tests get list of feature groups"() {
        given:
            FeatureGroupEntity featureGroup = FeatureGroupEntity.builder()
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            featureGroup.setActivations([
                new FeatureGroupActivationEntity(
                    featureGroup: featureGroup,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])

            featureGroup.features = [FeatureEntity.builder()
                                         .featureGroup(featureGroup)
                                         .key(textGenerator.generate())
                                         .name(textGenerator.generate())
                                         .description(textGenerator.generate())
                                         .build()
            ]

        and:
            FeatureGroupEntity createdFeatureGroup = this.sut.registerFeatureGroup(featureGroup)
            this.featureGroupPersistenceTestService.flushDatabase()

        when:
            List<FeatureGroupEntity> result = this.sut.getFeatureGroups()

        then:
            !result.empty
            result.first == createdFeatureGroup
    }

    void "Tests get feature group that exist"() {
        given:
            FeatureGroupEntity featureGroup = FeatureGroupEntity.builder()
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            featureGroup.setActivations([
                new FeatureGroupActivationEntity(
                    featureGroup: featureGroup,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])

            featureGroup.features = [FeatureEntity.builder()
                                         .featureGroup(featureGroup)
                                         .key(textGenerator.generate())
                                         .name(textGenerator.generate())
                                         .description(textGenerator.generate())
                                         .build()
            ]

        and:
            FeatureGroupEntity createdFeatureGroup = this.sut.registerFeatureGroup(featureGroup)
            this.featureGroupPersistenceTestService.flushDatabase()

        when:
            Optional<FeatureGroupEntity> foundFeatureGroup = this.sut.getFeatureGroup(featureGroup.getKey())

        then:
            !foundFeatureGroup.empty
            foundFeatureGroup.get() == createdFeatureGroup
    }

    void "Tests get feature group that does not exist"() {
        given:
            FeatureGroupEntity featureGroup = FeatureGroupEntity.builder()
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            featureGroup.setActivations([
                new FeatureGroupActivationEntity(
                    featureGroup: featureGroup,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])

            featureGroup.features = [FeatureEntity.builder()
                                         .featureGroup(featureGroup)
                                         .key(textGenerator.generate())
                                         .name(textGenerator.generate())
                                         .description(textGenerator.generate())
                                         .build()
            ]

        and:
            this.sut.registerFeatureGroup(featureGroup)
            this.featureGroupPersistenceTestService.flushDatabase()

        when:
            Optional<FeatureGroupEntity> foundFeatureGroup = this.sut.getFeatureGroup(featureGroup.getKey() + "-wrong-key")

        then:
            foundFeatureGroup.empty
    }

    void "Tests patch feature group that exist"() {
        given:
            FeatureGroupEntity featureGroup = FeatureGroupEntity.builder()
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            featureGroup.setActivations([
                new FeatureGroupActivationEntity(
                    featureGroup: featureGroup,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])

            featureGroup.features = [FeatureEntity.builder()
                                         .featureGroup(featureGroup)
                                         .key(textGenerator.generate())
                                         .name(textGenerator.generate())
                                         .description(textGenerator.generate())
                                         .build()
            ]

        and:
            FeatureGroupEntity createdFeatureGroup = this.sut.registerFeatureGroup(featureGroup)
            this.featureGroupPersistenceTestService.flushDatabase()

        when:
            Optional<FeatureGroupEntity> patchedFeatureGroup = this.sut.patchFeatureGroup(featureGroup.key, new FeatureGroupEntity(
                name: 'new-name',
                description: 'new-description'
            ))

        then:
            !patchedFeatureGroup.empty
            with(patchedFeatureGroup.get()) {
                assert it.key == createdFeatureGroup.key
                assert it.name == 'new-name'
                assert it.description == 'new-description'

                assert it.features.size() == createdFeatureGroup.features.size()
                assert it.activations.size() == createdFeatureGroup.activations.size()
            }
    }

    void "Tests patch feature group that does not exist"() {
        when:
            this.sut.patchFeatureGroup('key', new FeatureGroupEntity())

        then:
            ResourceNotFoundException exception = thrown(ResourceNotFoundException)
            exception.param == 'feature_group_key'
            exception.message == "No feature group with key 'key' exists"
    }

    void "Tests delete feature group that exist"() {
        given:
            FeatureGroupEntity featureGroup = FeatureGroupEntity.builder()
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            featureGroup.setActivations([
                new FeatureGroupActivationEntity(
                    featureGroup: featureGroup,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])

            featureGroup.features = [FeatureEntity.builder()
                                         .featureGroup(featureGroup)
                                         .key(textGenerator.generate())
                                         .name(textGenerator.generate())
                                         .description(textGenerator.generate())
                                         .build()
            ]

        and:
            FeatureGroupEntity createdFeatureGroup = this.sut.registerFeatureGroup(featureGroup)
            this.featureGroupPersistenceTestService.flushDatabase()

        when:
            this.sut.deleteFeatureGroup(featureGroup.getKey())
            this.featureGroupPersistenceTestService.flushDatabase()

        then:
            !this.featureGroupRepository.existsById(createdFeatureGroup.id)
    }

    void "Tests delete feature group that does not exist"() {
        when:
            this.sut.deleteFeatureGroup('some-key')

        then:
            ResourceNotFoundException exception = thrown(ResourceNotFoundException)
            exception.param == 'feature_group_key'
            exception.message == "No feature group with key 'some-key' exists"
    }

    void "Tests create activation for feature group that exists"() {
        given:
            FeatureGroupEntity featureGroup = FeatureGroupEntity.builder()
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            featureGroup.setActivations([
                new FeatureGroupActivationEntity(
                    featureGroup: featureGroup,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])

            featureGroup.features = [FeatureEntity.builder()
                                         .featureGroup(featureGroup)
                                         .key(textGenerator.generate())
                                         .name(textGenerator.generate())
                                         .description(textGenerator.generate())
                                         .build()
            ]

        and:
            FeatureGroupEntity createdFeatureGroup = this.sut.registerFeatureGroup(featureGroup)
            this.featureGroupPersistenceTestService.flushDatabase()

        and:
            FeatureGroupActivationEntity newActivation = new FeatureGroupActivationEntity(
                enabled: false,
                dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 3, 10, 14, 45), ZoneId.of('UTC'))
            )

        when:
            FeatureGroupActivationEntity createdActivation = this.sut.createActivation(featureGroup.key, newActivation)
            this.featureGroupPersistenceTestService.flushDatabase()

        then:
            createdActivation.id > 0
            createdActivation.featureGroup.id == createdFeatureGroup.id
            createdActivation.enabled == newActivation.enabled
            createdActivation.dateTime == newActivation.dateTime
    }

    void "Tests create activation for feature group that does not exist"() {
        given:
            FeatureGroupActivationEntity newActivation = new FeatureGroupActivationEntity(
                enabled: false,
                dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 3, 10, 14, 45), ZoneId.of('UTC'))
            )

        when:
            this.sut.createActivation('non-existing-key', newActivation)

        then:
            ResourceNotFoundException exception = thrown(ResourceNotFoundException)
            exception.param == 'feature_group_key'
            exception.message == "No feature group with key 'non-existing-key' exists"
    }

    void "Tests create activation for feature group with missing required fields"() {
        given:
            FeatureGroupEntity featureGroup = FeatureGroupEntity.builder()
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

            featureGroup.setActivations([
                new FeatureGroupActivationEntity(
                    featureGroup: featureGroup,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])

            featureGroup.features = [FeatureEntity.builder()
                                         .featureGroup(featureGroup)
                                         .key(textGenerator.generate())
                                         .name(textGenerator.generate())
                                         .description(textGenerator.generate())
                                         .build()
            ]

        and:
            FeatureGroupEntity createdFeatureGroup = this.sut.registerFeatureGroup(featureGroup)
            this.featureGroupPersistenceTestService.flushDatabase()

        and:
            FeatureGroupActivationEntity newActivation = new FeatureGroupActivationEntity(
                enabled: false
            )

        when:
            this.sut.createActivation(featureGroup.key, newActivation)
            this.featureGroupPersistenceTestService.flushDatabase()

        then:
            DataIntegrityViolationException ex = thrown(DataIntegrityViolationException)
            ex.message.contains('NULL not allowed for column "DATETIME";')
    }

}
