package dk.sunepoulsen.tes.features.service.domains.features

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureGroup
import dk.sunepoulsen.tes.features.model.FeatureGroup
import dk.sunepoulsen.tes.rest.models.EnvelopeModel
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

    void "Test get feature groups successfully"() {
        given:
            FeatureGroup model = new FeatureGroup(
                key: 'group-key'
            )

        when:
            DeferredResult<EnvelopeFeatureGroup> deferredResult = sut.getFeatureGroups()
            DeferredResults.wait(deferredResult)

        then:
            EnvelopeModel<FeatureGroup> modelResponse = deferredResult.result as EnvelopeModel<FeatureGroup>
            modelResponse.results.first.key == model.key

            1 * featureGroupsLogic.getFeatureGroups() >> CompletableFuture.completedFuture(new EnvelopeModel<FeatureGroup>(
                results: [model]
            ))
            0 * _
    }

    void "Test get feature groups return Future with LogicException"() {
        when:
            DeferredResult<EnvelopeFeatureGroup> deferredResult = sut.getFeatureGroups()
            DeferredResults.wait(deferredResult)
            throw deferredResult.getResult() as Throwable

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featureGroupsLogic.getFeatureGroups() >> CompletableFuture.failedFuture(
                new ResourceNotFoundException('code', 'param', 'message')
            )
            0 * _
    }

    void "Test get feature groups throws LogicException"() {
        when:
            sut.getFeatureGroups()

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featureGroupsLogic.getFeatureGroups() >> {
                throw new ResourceNotFoundException('code', 'param', 'message')
            }
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

    void "Test get feature group that return Future with LogicException"() {
        given:
            FeatureGroup model = new FeatureGroup(
                key: 'group-key'
            )

        when:
            DeferredResult<FeatureGroup> deferredResult = sut.getFeatureGroup(model.key)
            DeferredResults.wait(deferredResult)
            throw deferredResult.getResult() as Throwable

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featureGroupsLogic.getFeatureGroup(model.key) >> CompletableFuture.failedFuture(
                new ResourceNotFoundException('code', 'param', 'message')
            )
            0 * _
    }

    void "Test get feature group throws LogicException"() {
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

    void "Test patch feature group successfully"() {
        given:
            FeatureGroup newValues = new FeatureGroup(
                name: 'new-name'
            )

        when:
            DeferredResult<FeatureGroup> deferredResult = sut.patchFeatureGroup('key', newValues)
            DeferredResults.wait(deferredResult)

        then:
            FeatureGroup group = deferredResult.result as FeatureGroup
            group.key == 'returned-key'

            1 * featureGroupsLogic.patchFeatureGroup('key', newValues) >> CompletableFuture.completedFuture(new FeatureGroup(
                key: 'returned-key'
            ))
            0 * _
    }

    void "Test patch feature group that does not exist"() {
        given:
            FeatureGroup newValues = new FeatureGroup(
                name: 'new-name'
            )

        when:
            DeferredResult<FeatureGroup> deferredResult = sut.patchFeatureGroup('key', newValues)
            DeferredResults.wait(deferredResult)
            throw deferredResult.getResult() as Throwable

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featureGroupsLogic.patchFeatureGroup('key', newValues) >> CompletableFuture.failedFuture(
                new ResourceNotFoundException('code', 'param', 'message')
            )
            0 * _
    }

    void "Test patch feature group throws LogicException"() {
        given:
            FeatureGroup newValues = new FeatureGroup(
                name: 'new-name'
            )

        when:
            sut.patchFeatureGroup('key', newValues)

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featureGroupsLogic.patchFeatureGroup('key', newValues) >> {
                throw new ResourceNotFoundException('code', 'param', 'message')
            }
            0 * _
    }

    void "Test delete feature group successfully"() {
        when:
            DeferredResult<Void> deferredResult = sut.deleteFeatureGroup('key')
            DeferredResults.wait(deferredResult)

        then:
            deferredResult.result

            1 * featureGroupsLogic.deleteFeatureGroup('key') >> CompletableFuture.completedFuture(null)
            0 * _
    }

    void "Test delete feature group that does not exist"() {
        when:
            DeferredResult<Void> deferredResult = sut.deleteFeatureGroup('key')
            DeferredResults.wait(deferredResult)
            throw deferredResult.getResult() as Throwable

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featureGroupsLogic.deleteFeatureGroup('key') >> CompletableFuture.failedFuture(
                new ResourceNotFoundException('code', 'param', 'message')
            )
            0 * _
    }

    void "Test delete feature group throws LogicException"() {
        when:
            sut.deleteFeatureGroup('key')

        then:
            ApiNotFoundException ex = thrown(ApiNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * featureGroupsLogic.deleteFeatureGroup('key') >> {
                throw new ResourceNotFoundException('code', 'param', 'message')
            }
            0 * _
    }

}
