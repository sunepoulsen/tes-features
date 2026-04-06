package dk.sunepoulsen.tes.features.service.domains.features


import dk.sunepoulsen.tes.features.model.EnvelopeFeatureActivation
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiNotFoundException
import dk.sunepoulsen.tes.springboot.rest.logic.async.DeferredResults
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException
import org.springframework.web.context.request.async.DeferredResult
import spock.lang.Specification

import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture

class FeatureGroupActivationsControllerSpec extends Specification {

    private FeatureGroupsLogic featureGroupsLogic
    private FeatureGroupActivationsController sut

    void setup() {
        this.featureGroupsLogic = Mock(FeatureGroupsLogic)
        this.sut = new FeatureGroupActivationsController(this.featureGroupsLogic)
    }

    void "Test create new activation to feature group: Successfully"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true,
                datetime: ZonedDateTime.now()
            )

            FeatureActivation createdActivation = new FeatureActivation(
                id: 17L,
                enabled: true,
                datetime: ZonedDateTime.now()
            )

        when:
            DeferredResult<FeatureActivation> deferredResult = sut.createActivation('key', newActivation)
            DeferredResults.wait(deferredResult)

        then:
            FeatureActivation endpointResponse = deferredResult.result as FeatureActivation
            endpointResponse == createdActivation

            1 * featureGroupsLogic.createActivation('key', newActivation) >> CompletableFuture.completedFuture(createdActivation)
            0 * _
    }

    void "Test create new activation to feature group: Logic layer returns LogicException"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true,
                datetime: ZonedDateTime.now()
            )

        when:
            DeferredResult<FeatureActivation> deferredResult = sut.createActivation('key', newActivation)
            DeferredResults.wait(deferredResult)
            throw deferredResult.getResult() as Throwable

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featureGroupsLogic.createActivation('key', newActivation) >> CompletableFuture.failedFuture(
                new ResourceNotFoundException('code', 'param', 'message')
            )
            0 * _
    }

    void "Test create new activation to feature group: Logic layer throws LogicException"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true,
                datetime: ZonedDateTime.now()
            )

        when:
            sut.createActivation('key', newActivation)

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featureGroupsLogic.createActivation('key', newActivation) >> {
                throw new ResourceNotFoundException('code', 'param', 'message')
            }
            0 * _
    }

    void "Test get activations for feature group: Successfully"() {
        given:
            EnvelopeFeatureActivation envelope = new EnvelopeFeatureActivation()
            envelope.setResults([new FeatureActivation(id: 17L, enabled: true)])

        when:
            DeferredResult<EnvelopeFeatureActivation> deferredResult = sut.getActivations('key')
            DeferredResults.wait(deferredResult)

        then:
            EnvelopeFeatureActivation endpointResponse = deferredResult.result as EnvelopeFeatureActivation
            endpointResponse == envelope

            1 * featureGroupsLogic.getActivations('key') >> CompletableFuture.completedFuture(envelope)
            0 * _
    }

    void "Test get activations for feature group: Logic layer returns LogicException"() {
        when:
            DeferredResult<EnvelopeFeatureActivation> deferredResult = sut.getActivations('key')
            DeferredResults.wait(deferredResult)
            throw deferredResult.getResult() as Throwable

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featureGroupsLogic.getActivations('key') >> CompletableFuture.failedFuture(
                new ResourceNotFoundException('code', 'param', 'message')
            )
            0 * _
    }

    void "Test get activations for feature group: Logic layer throws LogicException"() {
        when:
            sut.getActivations('key')

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featureGroupsLogic.getActivations('key') >> {
                throw new ResourceNotFoundException('code', 'param', 'message')
            }
            0 * _
    }

}
