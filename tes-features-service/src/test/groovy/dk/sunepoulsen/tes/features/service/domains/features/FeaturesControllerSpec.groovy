package dk.sunepoulsen.tes.features.service.domains.features

import dk.sunepoulsen.tes.features.model.EnvelopeFeature
import dk.sunepoulsen.tes.features.model.Feature
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiConflictException
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiNotFoundException
import dk.sunepoulsen.tes.springboot.rest.logic.async.DeferredResults
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.DuplicateResourceException
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException
import org.springframework.web.context.request.async.DeferredResult
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class FeaturesControllerSpec extends Specification {

    private FeaturesLogic featuresLogic
    private FeaturesController sut

    void setup() {
        this.featuresLogic = Mock(FeaturesLogic)
        this.sut = new FeaturesController(this.featuresLogic)
    }

    void "Test register new features successfully"() {
        given:
            RegisterFeatureGroup model = new RegisterFeatureGroup()

        when:
            DeferredResult<RegisterFeatureGroup> deferredResult = sut.registerFeatures(model)
            DeferredResults.wait(deferredResult)

        then:
            RegisterFeatureGroup group = deferredResult.result as RegisterFeatureGroup
            group.key == 'group-key'

            1 * featuresLogic.registerFeatures(model) >> CompletableFuture.completedFuture(new RegisterFeatureGroup(
                key: 'group-key'
            ))
            0 * _
    }

    void "Test register new features throws LogicException"() {
        given:
            RegisterFeatureGroup model = new RegisterFeatureGroup()

        when:
            sut.registerFeatures(model)

        then:
            ApiConflictException ex = thrown(ApiConflictException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featuresLogic.registerFeatures(model) >> {
                throw new DuplicateResourceException('code', 'param', 'message')
            }
            0 * _
    }

    void "Test get features for a feature group successfully"() {
        given:
            Feature model = new Feature(
                key: 'group-key'
            )

        when:
            DeferredResult<EnvelopeFeature> deferredResult = sut.getFeatures('key')
            DeferredResults.wait(deferredResult)

        then:
            EnvelopeFeature modelResponse = deferredResult.result as EnvelopeFeature
            modelResponse.results.first.key == model.key

            1 * featuresLogic.getFeatures('key') >> CompletableFuture.completedFuture(new EnvelopeFeature(
                results: [model]
            ))
            0 * _
    }

    void "Test get features for a feature group that returns Future with LogicException"() {
        when:
            DeferredResult<EnvelopeFeature> deferredResult = sut.getFeatures('key')
            DeferredResults.wait(deferredResult)
            throw deferredResult.getResult() as Throwable

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featuresLogic.getFeatures('key') >> CompletableFuture.failedFuture(
                new ResourceNotFoundException('code', 'param', 'message')
            )
            0 * _
    }

    void "Test get features for a feature group that throws LogicException"() {
        when:
            sut.getFeatures('key')

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featuresLogic.getFeatures('key') >> {
                throw new ResourceNotFoundException('code', 'param', 'message')
            }
            0 * _
    }

    void "Test get feature for a feature group successfully"() {
        given:
            Feature model = new Feature(
                key: 'key'
            )

        when:
            DeferredResult<Feature> deferredResult = sut.getFeature('group-key', 'key')
            DeferredResults.wait(deferredResult)

        then:
            Feature modelResponse = deferredResult.result as Feature
            modelResponse.key == model.key

            1 * featuresLogic.getFeature('group-key', 'key') >> CompletableFuture.completedFuture(model)
            0 * _
    }

    void "Test get feature for a feature group that does not exist"() {
        when:
            DeferredResult<Feature> deferredResult = sut.getFeature('group-key', 'key')
            DeferredResults.wait(deferredResult)
            throw deferredResult.getResult() as Throwable

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featuresLogic.getFeature('group-key', 'key') >> CompletableFuture.failedFuture(
                new ResourceNotFoundException('code', 'param', 'message')
            )
            0 * _
    }

    void "Test get feature for a feature group fails with LogicException"() {
        when:
            sut.getFeature('group-key', 'key')

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featuresLogic.getFeature('group-key', 'key') >> {
                throw new ResourceNotFoundException('code', 'param', 'message')
            }
            0 * _
    }

    void "Test patch feature in a feature group successfully"() {
        given:
            Feature model = new Feature(
                key: 'key'
            )

            Feature newValues = new Feature(
                name: 'new-name'
            )

        when:
            DeferredResult<Feature> deferredResult = sut.patchFeature('group-key', 'key', newValues)
            DeferredResults.wait(deferredResult)

        then:
            Feature modelResponse = deferredResult.result as Feature
            modelResponse.key == model.key

            1 * featuresLogic.patchFeature('group-key', 'key', newValues) >> CompletableFuture.completedFuture(model)
            0 * _
    }

    void "Test patch feature in a feature group that does not exist"() {
        given:
            Feature newValues = new Feature(
                name: 'new-name'
            )

        when:
            DeferredResult<Feature> deferredResult = sut.patchFeature('group-key', 'key', newValues)
            DeferredResults.wait(deferredResult)
            throw deferredResult.getResult() as Throwable

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featuresLogic.patchFeature('group-key', 'key', newValues) >> CompletableFuture.failedFuture(
                new ResourceNotFoundException('code', 'param', 'message')
            )
            0 * _
    }

    void "Test patch feature in a feature group fails with LogicException"() {
        given:
            Feature newValues = new Feature(
                name: 'new-name'
            )

        when:
            sut.patchFeature('group-key', 'key', newValues)

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featuresLogic.patchFeature('group-key', 'key', newValues) >> {
                throw new ResourceNotFoundException('code', 'param', 'message')
            }
            0 * _
    }

}
