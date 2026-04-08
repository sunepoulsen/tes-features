package dk.sunepoulsen.tes.features.service.domains.features

import dk.sunepoulsen.tes.features.data.generators.FeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.model.EnvelopeFeatureActivation
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.features.model.FeatureGroup
import dk.sunepoulsen.tes.features.service.domains.persistence.FeatureGroupPersistence
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupActivationEntity
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity
import dk.sunepoulsen.tes.rest.models.EnvelopeModel
import dk.sunepoulsen.tes.rest.models.NoContent
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiNotFoundException
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class FeatureGroupsLogicSpec extends Specification {

    private FeatureGroupTransformations featureGroupTransformations
    private FeatureGroupPersistence featureGroupPersistence
    private FeatureGroupActivationTransformations featureGroupActivationTransformations
    private FeatureGroupsLogic sut

    void setup() {
        this.featureGroupTransformations = Mock(FeatureGroupTransformations)
        this.featureGroupPersistence = Mock(FeatureGroupPersistence)
        this.featureGroupActivationTransformations = Mock(FeatureGroupActivationTransformations)

        this.sut = new FeatureGroupsLogic(featureGroupTransformations, featureGroupPersistence, featureGroupActivationTransformations)
    }

    void "Test successful get feature groups"() {
        given:
            FeatureGroupEntity foundEntity = new FeatureGroupEntity()
            FeatureGroup foundFeatureGroup = new FeatureGroupDataGenerator().generate()

        when:
            CompletableFuture<EnvelopeModel<FeatureGroup>> result = sut.getFeatureGroups()

        then:
            !result.get().getResults().empty
            result.get().getResults().first == foundFeatureGroup

            1 * featureGroupPersistence.getFeatureGroups() >> [foundEntity]
            1 * featureGroupTransformations.toFeatureGroupModel(foundEntity) >> foundFeatureGroup
            0 * _
    }

    void "Test get feature groups with thrown exception"() {
        when:
            sut.getFeatureGroups().get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            exception.cause instanceof NullPointerException
            exception.cause.message == 'message'

            featureGroupPersistence.getFeatureGroups() >> {
                throw new NullPointerException('message')
            }
            0 * _
    }

    void "Test successful get a feature group that exist"() {
        given:
            FeatureGroupEntity foundEntity = new FeatureGroupEntity()
            FeatureGroup foundFeatureGroup = new FeatureGroupDataGenerator().generate()

        when:
            CompletableFuture<FeatureGroup> result = sut.getFeatureGroup('key')

        then:
            result.get() == foundFeatureGroup

            1 * featureGroupPersistence.getFeatureGroup('key') >> Optional.of(foundEntity)
            1 * featureGroupTransformations.toFeatureGroupModel(foundEntity) >> foundFeatureGroup
            0 * _
    }

    void "Test successful get a feature group that does not exist"() {
        when:
            sut.getFeatureGroup('key').get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ApiNotFoundException apiNotFoundException = exception.cause as ApiNotFoundException
            apiNotFoundException.serviceError.param == 'key'
            apiNotFoundException.serviceError.message == 'No feature group exists with key: key'

            1 * featureGroupPersistence.getFeatureGroup('key') >> Optional.empty()
            0 * _
    }

    void "Test get feature group with thrown exception"() {
        when:
            sut.getFeatureGroup('key').get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            exception.cause instanceof NullPointerException
            exception.cause.message == 'message'

            featureGroupPersistence.getFeatureGroup('key') >> {
                throw new NullPointerException('message')
            }
            0 * _
    }

    void "Test patch of a feature group that exists"() {
        given:
            FeatureGroup newValues = new FeatureGroup(
                name: 'new-name'
            )
            FeatureGroupEntity newValuesEntity = new FeatureGroupEntity(
                name: 'new-name'
            )

            FeatureGroupEntity patchedEntity = new FeatureGroupEntity(
                name: 'new-name',
                description: 'old-description'
            )
            FeatureGroup patchedFeatureGroup = new FeatureGroup(
                name: patchedEntity.name,
                description: patchedEntity.description
            )

        when:
            CompletableFuture<FeatureGroup> result = sut.patchFeatureGroup('key', newValues)

        then:
            result.get() == patchedFeatureGroup

            1 * featureGroupPersistence.patchFeatureGroup('key', newValuesEntity) >> Optional.of(patchedEntity)
            1 * featureGroupTransformations.toPatchEntity(newValues) >> newValuesEntity
            1 * featureGroupTransformations.toFeatureGroupModel(patchedEntity) >> patchedFeatureGroup
            0 * _
    }

    void "Test patch of a feature group that exists, but can not be returned"() {
        given:
            FeatureGroup newValues = new FeatureGroup(
                name: 'new-name'
            )
            FeatureGroupEntity newValuesEntity = new FeatureGroupEntity(
                name: 'new-name'
            )

        when:
            sut.patchFeatureGroup('key', newValues).get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ResourceNotFoundException resourceNotFoundException = exception.cause as ResourceNotFoundException
            resourceNotFoundException.param == 'key'
            resourceNotFoundException.message == "No feature group exists with key: key"

            1 * featureGroupPersistence.patchFeatureGroup('key', newValuesEntity) >> Optional.empty()
            1 * featureGroupTransformations.toPatchEntity(newValues) >> newValuesEntity
            0 * _
    }

    void "Test patch of a feature group that does not exist"() {
        given:
            FeatureGroup newValues = new FeatureGroup()
            FeatureGroupEntity newValuesEntity = new FeatureGroupEntity()

        when:
            sut.patchFeatureGroup('key', newValues).get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ResourceNotFoundException resourceNotFoundException = exception.cause as ResourceNotFoundException
            resourceNotFoundException.param == 'key'
            resourceNotFoundException.message == 'message'

            1 * featureGroupPersistence.patchFeatureGroup('key', newValuesEntity) >> {
                throw new ResourceNotFoundException('key', 'message')
            }
            1 * featureGroupTransformations.toPatchEntity(newValues) >> newValuesEntity
            0 * _
    }

    void "Test delete of a feature group that exists"() {
        when:
            NoContent result = sut.deleteFeatureGroup('key').get()

        then:
            result == new NoContent()

            1 * featureGroupPersistence.deleteFeatureGroup('key')
            0 * _
    }

    void "Test delete of a feature group that does not exists"() {
        when:
            sut.deleteFeatureGroup('key').get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ResourceNotFoundException resourceNotFoundException = exception.cause as ResourceNotFoundException
            resourceNotFoundException.param == 'key'
            resourceNotFoundException.message == 'message'

            1 * featureGroupPersistence.deleteFeatureGroup('key') >> {
                throw new ResourceNotFoundException('key', 'message')
            }
            0 * _
    }

    void "Test successful creation of a feature activation"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true
            )
            FeatureGroupActivationEntity activationEntity = new FeatureGroupActivationEntity(
                enabled: true
            )
            FeatureGroupActivationEntity createdEntity = new FeatureGroupActivationEntity(
                id: 1L,
                enabled: true
            )
            FeatureActivation createdActivation = new FeatureActivation(
                id: 1L,
                enabled: true
            )

        when:
            CompletableFuture<FeatureActivation> result = sut.createActivation('key', newActivation)

        then:
            result.get() == createdActivation

            1 * featureGroupActivationTransformations.toEntity(newActivation) >> activationEntity
            1 * featureGroupPersistence.createActivation('key', activationEntity) >> createdEntity
            1 * featureGroupActivationTransformations.toModel(createdEntity) >> createdActivation
            0 * _
    }

    void "Test creation of a feature activation with thrown exception"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true
            )
            FeatureGroupActivationEntity activationEntity = new FeatureGroupActivationEntity(
                enabled: true
            )

        when:
            sut.createActivation('key', newActivation).get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ResourceNotFoundException resourceNotFoundException = exception.cause as ResourceNotFoundException
            resourceNotFoundException.param == 'key'
            resourceNotFoundException.message == 'message'

            1 * featureGroupActivationTransformations.toEntity(newActivation) >> activationEntity
            1 * featureGroupPersistence.createActivation('key', activationEntity) >> {
                throw new ResourceNotFoundException('key', 'message')
            }
            0 * _
    }

    void "Test successful get activations for feature group"() {
        given:
            FeatureGroupActivationEntity foundEntity = new FeatureGroupActivationEntity(id: 1L, enabled: true)
            FeatureActivation foundActivation = new FeatureActivation(id: 1L, enabled: true)

        when:
            CompletableFuture<EnvelopeFeatureActivation> result = sut.getActivations('key')

        then:
            !result.get().results.empty
            result.get().results.first == foundActivation

            1 * featureGroupPersistence.getActivations('key') >> [foundEntity]
            1 * featureGroupActivationTransformations.toModel(foundEntity) >> foundActivation
            0 * _
    }

    void "Test get activations for feature group with thrown exception"() {
        when:
            sut.getActivations('key').get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ResourceNotFoundException resourceNotFoundException = exception.cause as ResourceNotFoundException
            resourceNotFoundException.param == 'key'
            resourceNotFoundException.message == 'message'

            1 * featureGroupPersistence.getActivations('key') >> {
                throw new ResourceNotFoundException('key', 'message')
            }
            0 * _
    }

    void "Test successful get specific activation for feature group"() {
        given:
            FeatureGroupActivationEntity foundEntity = new FeatureGroupActivationEntity(id: 1L, enabled: true)
            FeatureActivation foundActivation = new FeatureActivation(id: 1L, enabled: true)

        when:
            CompletableFuture<FeatureActivation> result = sut.getActivation('key', 1)

        then:
            result.get() == foundActivation

            1 * featureGroupPersistence.getActivation('key', 1) >> Optional.of(foundEntity)
            1 * featureGroupActivationTransformations.toModel(foundEntity) >> foundActivation
            0 * _
    }

    void "Test successful get specific activation for feature group that does not exist"() {
        when:
            sut.getActivation('key', 1).get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ApiNotFoundException apiNotFoundException = exception.cause as ApiNotFoundException
            apiNotFoundException.serviceError.param == 'activation_id'
            apiNotFoundException.serviceError.message == 'No activation exists with id: 1'

            1 * featureGroupPersistence.getActivation('key', 1) >> Optional.empty()
            0 * _
    }

    void "Test get specific activation for feature group with thrown exception"() {
        when:
            sut.getActivation('key', 1).get()

        then:
            ExecutionException exception = thrown(ExecutionException)
            ResourceNotFoundException resourceNotFoundException = exception.cause as ResourceNotFoundException
            resourceNotFoundException.param == 'key'
            resourceNotFoundException.message == 'message'

            1 * featureGroupPersistence.getActivation('key', 1) >> {
                throw new ResourceNotFoundException('key', 'message')
            }
            0 * _
    }

}
