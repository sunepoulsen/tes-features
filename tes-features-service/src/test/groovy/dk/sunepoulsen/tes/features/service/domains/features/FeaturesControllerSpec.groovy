package dk.sunepoulsen.tes.features.service.domains.features


import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiConflictException
import dk.sunepoulsen.tes.springboot.rest.logic.async.DeferredResults
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.DuplicateResourceException
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
    }

}
