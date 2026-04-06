package dk.sunepoulsen.tes.features.service.domains.features

import dk.sunepoulsen.tes.features.data.generators.FeatureDataGenerator
import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.model.*
import dk.sunepoulsen.tes.features.service.domains.persistence.FeatureGroupPersistence
import dk.sunepoulsen.tes.features.service.domains.persistence.FeaturePersistence
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureActivationEntity
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureEntity
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class FeaturesLogicSpec extends Specification {

    private FeatureGroupTransformations featureGroupTransformations
    private FeatureTransformations featureTransformations
    private FeatureGroupPersistence featureGroupPersistence
    private FeaturePersistence featurePersistence
    private FeatureActivationTransformations featureActivationTransformations
    private FeaturesLogic sut

    void setup() {
        this.featureGroupTransformations = Mock(FeatureGroupTransformations)
        this.featureTransformations = Mock(FeatureTransformations)
        this.featureGroupPersistence = Mock(FeatureGroupPersistence)
        this.featurePersistence = Mock(FeaturePersistence)
        this.featureActivationTransformations = Mock(FeatureActivationTransformations)

        this.sut = new FeaturesLogic(
            featureGroupTransformations,
            featureTransformations,
            featureGroupPersistence,
            featurePersistence,
            this.featureActivationTransformations
        )
    }

    void "Test successful register of features"() {
        given:
            RegisterFeatureGroup featureGroup = new RegisterFeatureGroupDataGenerator().generate()

            FeatureGroupEntity createEntity = new FeatureGroupEntity()
            FeatureGroupEntity createdEntity = new FeatureGroupEntity(id: 1L)
            RegisterFeatureGroup returnedFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        when:
            CompletableFuture<RegisterFeatureGroup> result = sut.registerFeatures(featureGroup)

        then:
            result.get() == returnedFeatureGroup

            1 * featureGroupTransformations.toEntity(featureGroup) >> createEntity
            1 * featureGroupPersistence.registerFeatureGroup(createEntity) >> createdEntity
            1 * featureGroupTransformations.toRegisterModel(createdEntity) >> returnedFeatureGroup
    }

    void "Test register of features with thrown exception"() {
        given:
            RegisterFeatureGroup featureGroup = new RegisterFeatureGroupDataGenerator().generate()

        when:
            sut.registerFeatures(featureGroup).get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            exception.cause instanceof NullPointerException
            exception.cause.message == 'message'

            1 * featureGroupTransformations.toEntity(featureGroup) >> {
                throw new NullPointerException('message')
            }
            0 * _
    }

    void "Test successful get feature"() {
        given:
            FeatureEntity foundEntity = new FeatureEntity()
            Feature foundFeature = new FeatureDataGenerator().generate()

        when:
            CompletableFuture<Feature> result = sut.getFeature('group-key', foundFeature.key)

        then:
            result.get().key == foundFeature.key

            1 * featurePersistence.getFeature('group-key', foundFeature.key) >> Optional.of(foundEntity)
            1 * featureTransformations.toFeatureModel(foundEntity) >> foundFeature
            0 * _
    }

    void "Test get feature that does not exist"() {
        when:
            sut.getFeature('group-key', 'key').get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            exception.cause instanceof ResourceNotFoundException
            exception.cause.message == 'No feature exists with the given keys'

            1 * featurePersistence.getFeature('group-key', 'key') >> Optional.empty()
            0 * _
    }

    void "Test get feature fails with exception"() {
        when:
            sut.getFeature('group-key', 'key').get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            exception.cause instanceof NullPointerException
            exception.cause.message == 'message'

            1 * featurePersistence.getFeature('group-key', 'key') >> {
                throw new NullPointerException('message')
            }
            0 * _
    }

    void "Test successful get features for a feature group"() {
        given:
            FeatureEntity foundEntity = new FeatureEntity()
            Feature foundFeature = new FeatureDataGenerator().generate()

        when:
            CompletableFuture<EnvelopeFeature> result = sut.getFeatures('key')

        then:
            !result.get().getResults().empty
            result.get().getResults().first == foundFeature

            1 * featurePersistence.getFeatures('key') >> [foundEntity]
            1 * featureTransformations.toFeatureModel(foundEntity) >> foundFeature
            0 * _
    }

    void "Test get features for a feature group with thrown exception"() {
        when:
            sut.getFeatures('key').get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            exception.cause instanceof NullPointerException
            exception.cause.message == 'message'

            1 * featurePersistence.getFeatures('key') >> {
                throw new NullPointerException('message')
            }
            0 * _
    }

    void "Test patch of a feature that exists"() {
        given:
            Feature newValues = new Feature(
                name: 'new-name'
            )
            FeatureEntity newValuesEntity = new FeatureEntity(
                name: 'new-name'
            )

            FeatureEntity patchedEntity = new FeatureEntity(
                name: 'new-name',
                description: 'old-description'
            )
            Feature patchedFeature = new Feature(
                name: patchedEntity.name,
                description: patchedEntity.description
            )

        when:
            CompletableFuture<Feature> result = sut.patchFeature('group-key', 'key', newValues)

        then:
            result.get() == patchedFeature

            1 * featurePersistence.patchFeature('group-key', 'key', newValuesEntity) >> Optional.of(patchedEntity)
            1 * featureTransformations.toPatchEntity(newValues) >> newValuesEntity
            1 * featureTransformations.toFeatureModel(patchedEntity) >> patchedFeature
            0 * _
    }

    void "Test patch of a feature group that exists, but can not be returned"() {
        given:
            Feature newValues = new Feature(
                name: 'new-name'
            )
            FeatureEntity newValuesEntity = new FeatureEntity(
                name: 'new-name'
            )

        when:
            sut.patchFeature('group-key', 'key', newValues).get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ResourceNotFoundException resourceNotFoundException = exception.cause as ResourceNotFoundException
            resourceNotFoundException.message == "No feature with feature group 'group-key' and feature 'key' exists"

            1 * featurePersistence.patchFeature('group-key', 'key', newValuesEntity) >> Optional.empty()
            1 * featureTransformations.toPatchEntity(newValues) >> newValuesEntity
            0 * _
    }

    void "Test patch of a feature group that does not exist"() {
        given:
            Feature newValues = new Feature()
            FeatureEntity newValuesEntity = new FeatureEntity()

        when:
            sut.patchFeature('group-key', 'key', newValues).get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ResourceNotFoundException resourceNotFoundException = exception.cause as ResourceNotFoundException
            resourceNotFoundException.param == 'key'
            resourceNotFoundException.message == 'message'

            1 * featurePersistence.patchFeature('group-key', 'key', newValuesEntity) >> {
                throw new ResourceNotFoundException('key', 'message')
            }
            1 * featureTransformations.toPatchEntity(newValues) >> newValuesEntity
            0 * _
    }

    void "Test successful delete of a feature"() {
        when:
            CompletableFuture result = sut.deleteFeature('group-key', 'key')

        then:
            result.get() != null

            1 * featurePersistence.deleteFeature('group-key', 'key')
            0 * _
    }

    void "Test delete of a feature with thrown exception"() {
        when:
            sut.deleteFeature('group-key', 'key').get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            exception.cause instanceof NullPointerException
            exception.cause.message == 'message'

            1 * featurePersistence.deleteFeature('group-key', 'key') >> {
                throw new NullPointerException('message')
            }
            0 * _
    }

    void "Test create new activation successfully"() {
        given:
            FeatureActivation newActivation = new FeatureActivation()
            FeatureActivationEntity activationEntity = new FeatureActivationEntity()
            FeatureActivationEntity createdEntity = new FeatureActivationEntity()
            FeatureActivation returnedActivation = new FeatureActivation()

        when:
            CompletableFuture<FeatureActivation> result = sut.createActivation('group-key', 'feature-key', newActivation)

        then:
            result.get() == returnedActivation

            1 * featureActivationTransformations.toEntity(newActivation) >> activationEntity
            1 * featurePersistence.createActivation('group-key', 'feature-key', activationEntity) >> createdEntity
            1 * featureActivationTransformations.toModel(createdEntity) >> returnedActivation
            0 * _
    }

    void "Test create new activation with thrown exception"() {
        given:
            FeatureActivation newActivation = new FeatureActivation()
            FeatureActivationEntity activationEntity = new FeatureActivationEntity()

        when:
            sut.createActivation('group-key', 'feature-key', newActivation).get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ResourceNotFoundException resourceNotFoundException = exception.cause as ResourceNotFoundException
            resourceNotFoundException.param == 'key'
            resourceNotFoundException.message == 'message'

            1 * featureActivationTransformations.toEntity(newActivation) >> activationEntity
            1 * featurePersistence.createActivation('group-key', 'feature-key', activationEntity) >> {
                throw new ResourceNotFoundException('key', 'message')
            }
            0 * _
    }

    void "Test successful get activations for feature"() {
        given:
            FeatureActivationEntity foundEntity = new FeatureActivationEntity(id: 1L, enabled: true)
            FeatureActivation foundActivation = new FeatureActivation(id: 1L, enabled: true)

        when:
            CompletableFuture<EnvelopeFeatureActivation> result = sut.getActivations('group-key', 'feature-key')

        then:
            !result.get().results.empty
            result.get().results.first == foundActivation

            1 * featurePersistence.getActivations('group-key', 'feature-key') >> [foundEntity]
            1 * featureActivationTransformations.toModel(foundEntity) >> foundActivation
            0 * _
    }

    void "Test get activations for feature with thrown exception"() {
        when:
            sut.getActivations('group-key', 'feature-key').get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ResourceNotFoundException resourceNotFoundException = exception.cause as ResourceNotFoundException
            resourceNotFoundException.param == 'key'
            resourceNotFoundException.message == 'message'

            1 * featurePersistence.getActivations('group-key', 'feature-key') >> {
                throw new ResourceNotFoundException('key', 'message')
            }
            0 * _
    }

    void "Test successful get specific activation for feature"() {
        given:
            FeatureActivationEntity foundEntity = new FeatureActivationEntity(id: 1L, enabled: true)
            FeatureActivation foundActivation = new FeatureActivation(id: 1L, enabled: true)

        when:
            CompletableFuture<FeatureActivation> result = sut.getActivation('group-key', 'feature-key', 1)

        then:
            result.get() == foundActivation

            1 * featurePersistence.getActivation('group-key', 'feature-key', 1) >> Optional.of(foundEntity)
            1 * featureActivationTransformations.toModel(foundEntity) >> foundActivation
            0 * _
    }

    void "Test successful get specific activation for feature that does not exist"() {
        when:
            sut.getActivation('group-key', 'feature-key', 1).get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ResourceNotFoundException resourceNotFoundException = exception.cause as ResourceNotFoundException
            resourceNotFoundException.message == "No activation exists with the given keys and id: 1"

            1 * featurePersistence.getActivation('group-key', 'feature-key', 1) >> Optional.empty()
            0 * _
    }

    void "Test get specific activation for feature with thrown exception"() {
        when:
            sut.getActivation('group-key', 'feature-key', 1).get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ResourceNotFoundException resourceNotFoundException = exception.cause as ResourceNotFoundException
            resourceNotFoundException.param == 'key'
            resourceNotFoundException.message == 'message'

            1 * featurePersistence.getActivation('group-key', 'feature-key', 1) >> {
                throw new ResourceNotFoundException('key', 'message')
            }
            0 * _
    }

}
