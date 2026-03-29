package dk.sunepoulsen.tes.features.service.domains.features


import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiNotFoundException
import dk.sunepoulsen.tes.springboot.rest.logic.async.DeferredResults
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException
import org.springframework.web.context.request.async.DeferredResult
import spock.lang.Specification

import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture

class FeaturesActivationsControllerSpec extends Specification {

    private FeaturesLogic featuresLogic
    private FeaturesActivationsController sut

    void setup() {
        this.featuresLogic = Mock(FeaturesLogic)
        this.sut = new FeaturesActivationsController(this.featuresLogic)
    }

    void "Test create new activation to a feature: Successfully"() {
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
            DeferredResult<FeatureActivation> deferredResult = sut.createActivation('group-key', 'feature-key', newActivation)
            DeferredResults.wait(deferredResult)

        then:
            FeatureActivation endpointResponse = deferredResult.result as FeatureActivation
            endpointResponse == createdActivation

            1 * featuresLogic.createActivation('group-key', 'feature-key', newActivation) >> CompletableFuture.completedFuture(createdActivation)
            0 * _
    }

    void "Test create new activation to a feature: Logic layer returns LogicException"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true,
                datetime: ZonedDateTime.now()
            )

        when:
            DeferredResult<FeatureActivation> deferredResult = sut.createActivation('group-key', 'feature-key', newActivation)
            DeferredResults.wait(deferredResult)
            throw deferredResult.getResult() as Throwable

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featuresLogic.createActivation('group-key', 'feature-key', newActivation) >> CompletableFuture.failedFuture(
                new ResourceNotFoundException('code', 'param', 'message')
            )
            0 * _
    }

    void "Test create new activation to a feature: Logic layer throws LogicException"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true,
                datetime: ZonedDateTime.now()
            )

        when:
            sut.createActivation('group-key', 'feature-key', newActivation)

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featuresLogic.createActivation('group-key', 'feature-key', newActivation) >> {
                throw new ResourceNotFoundException('code', 'param', 'message')
            }
            0 * _
    }

}
