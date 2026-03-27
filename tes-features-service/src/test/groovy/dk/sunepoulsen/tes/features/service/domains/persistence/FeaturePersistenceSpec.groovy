package dk.sunepoulsen.tes.features.service.domains.persistence

import dk.sunepoulsen.tes.data.generators.DataGenerator
import dk.sunepoulsen.tes.data.generators.Generators
import dk.sunepoulsen.tes.data.generators.NumberGenerators
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureActivationEntity
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupActivationEntity
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity
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
class FeaturePersistenceSpec extends Specification {

    @Autowired
    private FeatureGroupRepository featureGroupRepository

    @Autowired
    private FeatureRepository featureRepository

    @Autowired
    private FeatureActivationRepository featureActivationRepository

    @Autowired
    private FeatureGroupPersistence featureGroupPersistence

    @Autowired
    private FeatureGroupPersistenceTestService featureGroupPersistenceTestService

    @Autowired
    private FeaturePersistence sut

    private DataGenerator<String> textGenerator

    void setup() {
        this.textGenerator = Generators.textGenerator(NumberGenerators.integerGenerator(10, 50))
        this.featureGroupPersistenceTestService.deleteAll()
    }

    void "Tests register of feature with no feature group"() {
        when:
            this.sut.registerFeature(FeatureEntity.builder()
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()
            )
            this.featureGroupPersistenceTestService.flushDatabase()

        then:
            IllegalArgumentException ex = thrown(IllegalArgumentException)
            ex.message == 'Feature group must not be null'

            featureGroupRepository.count() == 0
            featureRepository.count() == 0
            featureActivationRepository.count() == 0
    }

    void "Tests register of feature with feature group with no id"() {
        when:
            this.sut.registerFeature(FeatureEntity.builder()
                .featureGroup(FeatureGroupEntity.builder().build())
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()
            )
            this.featureGroupPersistenceTestService.flushDatabase()

        then:
            IllegalArgumentException ex = thrown(IllegalArgumentException)
            ex.message == 'Feature group id must not be null'

            featureGroupRepository.count() == 0
            featureRepository.count() == 0
            featureActivationRepository.count() == 0
    }

    void "Tests register of feature with feature group that does not exist"() {
        when:
            this.sut.registerFeature(FeatureEntity.builder()
                .featureGroup(FeatureGroupEntity.builder()
                    .id(27L)
                    .key(textGenerator.generate())
                    .build()
                )
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()
            )
            this.featureGroupPersistenceTestService.flushDatabase()

        then:
            ResourceNotFoundException ex = thrown(ResourceNotFoundException)
            ex.param == 'featureGroup'
            ex.message == 'Feature group 27 does not exist'

            featureGroupRepository.count() == 0
            featureRepository.count() == 0
            featureActivationRepository.count() == 0
    }

    void "Tests register of feature successfully with feature that does not exist"() {
        given:
            FeatureGroupEntity featureGroupEntity = featureGroupRepository.save(FeatureGroupEntity.builder()
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .key(textGenerator.generate())
                .build()
            )

            FeatureEntity feature = FeatureEntity.builder()
                .featureGroup(featureGroupEntity)
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()
            feature.setActivations([
                new FeatureActivationEntity(
                    feature: feature,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])

        when:
            FeatureEntity createdFeature = featureRepository.findById(this.sut.registerFeature(feature).id).get()
            this.featureGroupPersistenceTestService.flushDatabase()

        then:
            createdFeature.id > 0
            createdFeature.featureGroup == featureGroupEntity
            createdFeature.key == feature.key
            createdFeature.name == feature.name
            createdFeature.description == feature.description

            featureGroupRepository.count() == 1
            featureRepository.count() == 1
            featureActivationRepository.count() == 1

            List<FeatureActivationEntity> featureActivations = featureActivationRepository.findAllByFeatureId(createdFeature.id)
            featureActivations.size() == 1

            FeatureActivationEntity featureActivation = featureActivations.first
            featureActivation.id > 0
            featureActivation.feature.id == createdFeature.id
            featureActivation.enabled == feature.activations.first.enabled
            featureActivation.dateTime == feature.activations.first.dateTime
    }

    void "Tests register of feature successfully with feature that does exist"() {
        given:
            FeatureGroupEntity featureGroupEntity = FeatureGroupEntity.builder()
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .key(textGenerator.generate())
                .build()

            FeatureEntity existingFeature = FeatureEntity.builder()
                .featureGroup(featureGroupEntity)
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()
            existingFeature.setActivations([
                new FeatureActivationEntity(
                    feature: existingFeature,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                )
            ])
            featureGroupEntity.setFeatures([existingFeature])
            featureGroupEntity = featureGroupRepository.save(featureGroupEntity)

        and: 'verify feature group and feature in database'
            featureGroupEntity = featureGroupRepository.findById(featureGroupEntity.id).get()
            featureGroupEntity.id > 0
            featureGroupEntity.features.size() == 1
            featureGroupEntity.features.first.id > 0
            featureGroupEntity.features.first.activations.size() == 1
            featureGroupEntity.features.first.activations.first.id > 0

            existingFeature = featureGroupEntity.features.first

        and: 'setup new feature'
            FeatureEntity newFeature = FeatureEntity.builder()
                .featureGroup(featureGroupEntity)
                .key(existingFeature.key)
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()
            newFeature.setActivations([
                new FeatureActivationEntity(
                    feature: existingFeature,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2023, 2, 9, 1, 30), ZoneId.of('UTC'))
                )
            ])

        when:
            FeatureEntity createdFeature = featureRepository.findById(this.sut.registerFeature(newFeature).id).get()
            this.featureGroupPersistenceTestService.flushDatabase()

        then:
            createdFeature == existingFeature

            featureGroupRepository.count() == 1
            featureRepository.count() == 1
            featureActivationRepository.count() == 1

            List<FeatureActivationEntity> featureActivations = featureActivationRepository.findAllByFeatureId(createdFeature.id)
            featureActivations.size() == 1

            FeatureActivationEntity featureActivation = featureActivations.first
            featureActivation.id > 0
            featureActivation.feature.id == createdFeature.id
            featureActivation.enabled == existingFeature.activations.first.enabled
            featureActivation.dateTime == existingFeature.activations.first.dateTime
    }

    void "Tests register of feature with missing field"() {
        given:
            FeatureGroupEntity featureGroupEntity = featureGroupRepository.save(FeatureGroupEntity.builder()
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .key(textGenerator.generate())
                .build()
            )

            FeatureEntity feature = FeatureEntity.builder()
                .featureGroup(featureGroupEntity)
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .build()
            feature.setActivations(
                List.of(new FeatureActivationEntity(
                    feature: feature,
                    enabled: true,
                    dateTime: ZonedDateTime.of(LocalDateTime.of(2025, 2, 8, 12, 30), ZoneId.of('UTC'))
                ))
            )

        when:
            this.sut.registerFeature(feature)
            this.featureGroupPersistenceTestService.flushDatabase()

        then:
            DataIntegrityViolationException ex = thrown(DataIntegrityViolationException)
            ex.message.contains('NULL not allowed for column "DESCRIPTION";')
    }

    void "Tests get all features in a feature group that exists"() {
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
            FeatureGroupEntity createdFeatureGroup = featureGroupPersistence.registerFeatureGroup(featureGroup)

        when:
            List<FeatureEntity> result = sut.getFeatures(createdFeatureGroup.key)

        then:
            result.size() == 1
            result.first.key == createdFeatureGroup.features.first.key

    }

    void "Tests get all features that belong to a feature group that does not exist"() {
        when:
            sut.getFeatures('unknown')

        then:
            ResourceNotFoundException exception = thrown(ResourceNotFoundException)
            exception.param == 'feature_group_key'
            exception.message == "No feature group with key 'unknown' exists"
    }

    void "Tests get feature in a feature group that does not exists"() {
        when:
            Optional<FeatureEntity> result = sut.getFeature('wrong-group-key', 'wrong-key')

        then:
            result.empty
    }

    void "Tests get unknown feature in a feature group that exists"() {
        given:
            FeatureGroupEntity featureGroupEntity = featureGroupRepository.save(FeatureGroupEntity.builder()
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()
            )

            FeatureEntity feature = FeatureEntity.builder()
                .featureGroup(featureGroupEntity)
                .key(textGenerator.generate())
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

        and:
            this.sut.registerFeature(feature)
            this.featureGroupPersistenceTestService.flushDatabase()

        when:
            Optional<FeatureEntity> result = sut.getFeature(featureGroupEntity.key, 'wrong-key')

        then:
            result.empty
    }

    @Unroll
    void "Tests get known feature in a feature group that exists: #_testcase"() {
        given:
            FeatureGroupEntity featureGroupEntity = featureGroupRepository.save(FeatureGroupEntity.builder()
                .key(_featureGroupKey)
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()
            )

            FeatureEntity featureEntity = FeatureEntity.builder()
                .featureGroup(featureGroupEntity)
                .key(_featureKey)
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

        and:
            FeatureEntity createdFeature = featureRepository.findById(this.sut.registerFeature(featureEntity).id).get()
            this.featureGroupPersistenceTestService.flushDatabase()

        when:
            Optional<FeatureEntity> result = sut.getFeature(_argFeatureGroupKey, _argFeatureKey)

        then:
            !result.empty
            result.get().id == createdFeature.id
            result.get().key == createdFeature.key

        where:
            _testcase                         | _featureGroupKey  | _featureKey  | _argFeatureGroupKey             | _argFeatureKey
            'Normal case'                     | 'featureGroupKey' | 'featureKey' | 'featureGroupKey'               | 'featureKey'
            'Feature group key is lower case' | 'featureGroupKey' | 'featureKey' | 'featureGroupKey'.toLowerCase() | 'featureKey'
            'Feature group key is upper case' | 'featureGroupKey' | 'featureKey' | 'featureGroupKey'.toUpperCase() | 'featureKey'
            'Feature key is lower case'       | 'featureGroupKey' | 'featureKey' | 'featureGroupKey'               | 'featureKey'.toLowerCase()
            'Feature key is upper case'       | 'featureGroupKey' | 'featureKey' | 'featureGroupKey'               | 'featureKey'.toUpperCase()
    }

    @Unroll
    void "Tests patch feature that exist: #_testcase"() {
        given:
            FeatureGroupEntity featureGroupEntity = featureGroupRepository.save(FeatureGroupEntity.builder()
                .key(_featureGroupKey)
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()
            )

            FeatureEntity featureEntity = FeatureEntity.builder()
                .featureGroup(featureGroupEntity)
                .key(_featureKey)
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

        and:
            FeatureEntity createdFeature = featureRepository.findById(this.sut.registerFeature(featureEntity).id).get()
            this.featureGroupPersistenceTestService.flushDatabase()

        when:
            Optional<FeatureEntity> patchedFeature = this.sut.patchFeature(_argFeatureGroupKey, _argFeatureKey, new FeatureEntity(
                name: 'new-name',
                description: 'new-description'
            ))

        then:
            !patchedFeature.empty
            with(patchedFeature.get()) {
                assert it.key == createdFeature.key
                assert it.name == 'new-name'
                assert it.description == 'new-description'
            }

        where:
            _testcase                         | _featureGroupKey  | _featureKey  | _argFeatureGroupKey             | _argFeatureKey
            'Normal case'                     | 'featureGroupKey' | 'featureKey' | 'featureGroupKey'               | 'featureKey'
            'Feature group key is lower case' | 'featureGroupKey' | 'featureKey' | 'featureGroupKey'.toLowerCase() | 'featureKey'
            'Feature group key is upper case' | 'featureGroupKey' | 'featureKey' | 'featureGroupKey'.toUpperCase() | 'featureKey'
            'Feature key is lower case'       | 'featureGroupKey' | 'featureKey' | 'featureGroupKey'               | 'featureKey'.toLowerCase()
            'Feature key is upper case'       | 'featureGroupKey' | 'featureKey' | 'featureGroupKey'               | 'featureKey'.toUpperCase()
    }

    @Unroll
    void "Tests patch feature of missing feature: #_testcase"() {
        given:
            FeatureGroupEntity featureGroupEntity = featureGroupRepository.save(FeatureGroupEntity.builder()
                .key(_featureGroupKey)
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()
            )

            FeatureEntity featureEntity = FeatureEntity.builder()
                .featureGroup(featureGroupEntity)
                .key(_featureKey)
                .name(textGenerator.generate())
                .description(textGenerator.generate())
                .build()

        and:
            this.sut.registerFeature(featureEntity)
            this.featureGroupPersistenceTestService.flushDatabase()

        when:
            this.sut.patchFeature(_argFeatureGroupKey, _argFeatureKey, new FeatureEntity())

        then:
            ResourceNotFoundException exception = thrown(ResourceNotFoundException)
            exception.param == null
            exception.message == "No feature with feature group '${_argFeatureGroupKey}' and feature '${_argFeatureKey}' exists"

        where:
            _testcase                      | _featureGroupKey  | _featureKey  | _argFeatureGroupKey | _argFeatureKey
            'Feature group does not exist' | 'featureGroupKey' | 'featureKey' | 'bad-key'           | 'featureKey'
            'Feature does not exist'       | 'featureGroupKey' | 'featureKey' | 'featureGroupKey'   | 'bad-key'
    }

}
