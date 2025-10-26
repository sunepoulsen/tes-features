package dk.sunepoulsen.tes.features.service.domains.features

import dk.sunepoulsen.tes.features.model.FeatureGroup
import dk.sunepoulsen.tes.springboot.rest.exceptions.ApiNotFoundException
import dk.sunepoulsen.tes.springboot.rest.logic.async.DeferredResults
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.ResourceNotFoundException
import org.springframework.web.context.request.async.DeferredResult
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class FeatureGroupsControllerSpec extends Specification {

    private FeatureGroupsLogic featureGroupsLogic
    private FeatureGroupsController sut

    void setup() {
        this.featureGroupsLogic = Mock(FeatureGroupsLogic)
        this.sut = new FeatureGroupsController(this.featureGroupsLogic)
    }

    void "Test get feature group successfully"() {
        given:
            FeatureGroup model = new FeatureGroup(
                key: 'group-key'
            )

        when:
            DeferredResult<FeatureGroup> deferredResult = sut.getFeatureGroup(model.key)
            DeferredResults.wait(deferredResult)

        then:
            FeatureGroup group = deferredResult.result as FeatureGroup
            group.key == model.key

            1 * featureGroupsLogic.getFeatureGroup(model.key) >> CompletableFuture.completedFuture(new FeatureGroup(
                key: model.key
            ))
            0 * _
    }

    void "Test register new features throws LogicException"() {
        when:
            sut.getFeatureGroup('key')

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featureGroupsLogic.getFeatureGroup('key') >> {
                throw new ResourceNotFoundException('code', 'param', 'message')
            }
    }

}
