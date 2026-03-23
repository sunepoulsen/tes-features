package dk.sunepoulsen.tes.features.service.domains.features

import dk.sunepoulsen.tes.features.data.generators.FeatureDataGenerator
import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.model.EnvelopeFeature
import dk.sunepoulsen.tes.features.model.Feature
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.features.service.domains.persistence.FeatureGroupPersistence
import dk.sunepoulsen.tes.features.service.domains.persistence.FeaturePersistence
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
    private FeaturesLogic sut

    void setup() {
        this.featureGroupTransformations = Mock(FeatureGroupTransformations)
        this.featureTransformations = Mock(FeatureTransformations)
        this.featureGroupPersistence = Mock(FeatureGroupPersistence)
        this.featurePersistence = Mock(FeaturePersistence)

        this.sut = new FeaturesLogic(
            featureGroupTransformations,
            featureTransformations,
            featureGroupPersistence,
            featurePersistence
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

}
