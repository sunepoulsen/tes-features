package dk.sunepoulsen.tes.features.service.domains.features

import dk.sunepoulsen.tes.features.data.generators.FeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.model.FeatureGroup
import dk.sunepoulsen.tes.features.service.domains.persistence.FeatureGroupPersistence
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity
import dk.sunepoulsen.tes.rest.models.EnvelopeModel
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiNotFoundException
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class FeatureGroupsLogicSpec extends Specification {

    private FeatureGroupTransformations featureGroupTransformations
    private FeatureGroupPersistence featureGroupPersistence
    private FeatureGroupsLogic sut

    void setup() {
        this.featureGroupTransformations = Mock(FeatureGroupTransformations)
        this.featureGroupPersistence = Mock(FeatureGroupPersistence)

        this.sut = new FeatureGroupsLogic(featureGroupTransformations, featureGroupPersistence)
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

}
