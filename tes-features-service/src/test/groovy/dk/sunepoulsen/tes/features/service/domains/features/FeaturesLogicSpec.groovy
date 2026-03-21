package dk.sunepoulsen.tes.features.service.domains.features

import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.features.service.domains.persistence.FeatureGroupPersistence
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureGroupEntity
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class FeaturesLogicSpec extends Specification {

    private FeatureGroupTransformations featureGroupTransformations
    private FeatureGroupPersistence featureGroupPersistence
    private FeaturesLogic sut

    void setup() {
        this.featureGroupTransformations = Mock(FeatureGroupTransformations)
        this.featureGroupPersistence = Mock(FeatureGroupPersistence)

        this.sut = new FeaturesLogic(featureGroupTransformations, featureGroupPersistence)
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

}
