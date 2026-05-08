package dk.sunepoulsen.tes.features.integrator


import dk.sunepoulsen.tes.features.model.EnvelopeFeatureGroup
import dk.sunepoulsen.tes.features.model.FeatureGroup
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientInternalServerException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import io.reactivex.rxjava3.core.Single
import spock.lang.Specification

import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class FeatureGroupsIntegratorSpec extends Specification {

    private TechEasySolutionsClient httpClient
    private FeatureGroupsIntegrator sut

    void setup() {
        this.httpClient = Mock(TechEasySolutionsClient)
        this.sut = new FeatureGroupsIntegrator(this.httpClient)
    }

    void "Get feature groups with OK"() {
        when:
            Single<EnvelopeFeatureGroup> result = sut.getFeatureGroups('token')

        then:
            result.blockingGet().results.first.key == 'group-key'

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}", 'token', EnvelopeFeatureGroup) >> CompletableFuture.supplyAsync {
                new EnvelopeFeatureGroup(
                    results: [
                        new FeatureGroup(
                            key: 'group-key'
                        )
                    ]
                )
            }
            0 * _
    }

    void "Get feature groups returns Internal Server Error"() {
        when:
            sut.getFeatureGroups('token').blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}", 'token', EnvelopeFeatureGroup) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Get feature group with OK"() {
        when:
            Single<FeatureGroup> result = sut.getFeatureGroup('token', 'group-key')

        then:
            result.blockingGet().key == 'group-key'

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key", 'token', FeatureGroup) >> CompletableFuture.supplyAsync {
                new FeatureGroup(
                    key: 'group-key'
                )
            }
            0 * _
    }

    void "Get feature group Internal Server Error"() {
        when:
            sut.getFeatureGroup('token', 'group-key').blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key", 'token', FeatureGroup) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Patch feature group with OK"() {
        given:
            FeatureGroup newValues = new FeatureGroup(
                name: 'new-name'
            )

        when:
            Single<FeatureGroup> result = sut.patchFeatureGroup('token', 'group-key', newValues)

        then:
            result.blockingGet().key == 'group-key'

            1 * httpClient.patch("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key", 'token', newValues, FeatureGroup) >> CompletableFuture.supplyAsync {
                new FeatureGroup(
                    key: 'group-key'
                )
            }
            0 * _
    }

    void "Patch feature group Internal Server Error"() {
        given:
            FeatureGroup newValues = new FeatureGroup(
                name: 'new-name'
            )

        when:
            sut.patchFeatureGroup('token', 'group-key', newValues).blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.patch("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key", 'token', newValues, FeatureGroup) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Delete feature group with OK"() {
        when:
            sut.deleteFeatureGroup('token', 'group-key').blockingAwait()

        then:
            noExceptionThrown()

            1 * httpClient.delete("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key", 'token') >> CompletableFuture.completedFuture(null)
            0 * _
    }

    void "Delete feature group with not found"() {
        when:
            sut.deleteFeatureGroup('token', 'group-key').blockingAwait()

        then:
            ClientNotFoundException ex = thrown(ClientNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.delete("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key", 'token') >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientNotFoundException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
            0 * _
    }

}
