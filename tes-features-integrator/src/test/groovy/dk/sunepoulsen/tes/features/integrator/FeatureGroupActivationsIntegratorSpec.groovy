package dk.sunepoulsen.tes.features.integrator

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureActivation
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientInternalServerException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import io.reactivex.rxjava3.core.Single
import spock.lang.Specification

import java.net.http.HttpResponse
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class FeatureGroupActivationsIntegratorSpec extends Specification {

    private TechEasySolutionsClient httpClient
    private FeatureGroupActivationsIntegrator sut

    void setup() {
        this.httpClient = Mock(TechEasySolutionsClient)
        this.sut = new FeatureGroupActivationsIntegrator(this.httpClient)
    }

    void "Create new activation for a feature group with OK"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true,
                datetime: ZonedDateTime.now()
            )

        when:
            Single<FeatureActivation> result = sut.createFeatureGroupActivation('token', 'group-key', newActivation)

        then:
            result.blockingGet().id == 27L

            1 * httpClient.post("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/activations", 'token', newActivation, FeatureActivation) >> CompletableFuture.supplyAsync {
                new FeatureActivation(
                    id: 27L,
                    enabled: true,
                    datetime: ZonedDateTime.now()
                )
            }
            0 * _
    }

    void "Create new activation for a feature group with Internal Server Error"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true,
                datetime: ZonedDateTime.now()
            )

        when:
            sut.createFeatureGroupActivation('token', 'group-key', newActivation).blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.post("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/activations", 'token', newActivation, FeatureActivation) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Get feature group activations returns OK"() {
        when:
            Single<EnvelopeFeatureActivation> result = sut.getFeatureGroupActivations('token', 'group-key')

        then:
            result.blockingGet().results.first.id == 27L

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/activations", 'token', EnvelopeFeatureActivation) >> CompletableFuture.supplyAsync {
                new EnvelopeFeatureActivation(
                    results: [
                        new FeatureActivation(
                            id: 27L,
                            enabled: true,
                            datetime: ZonedDateTime.now()
                        )
                    ]
                )
            }
            0 * _
    }

    void "Get feature group activations returns Internal Server Error"() {
        when:
            sut.getFeatureGroupActivations('token', 'group-key').blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/activations", 'token', EnvelopeFeatureActivation) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Get feature group activation returns OK"() {
        when:
            Single<FeatureActivation> result = sut.getFeatureGroupActivation('token', 'group-key', 27L)

        then:
            result.blockingGet().id == 27L

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/activations/27", 'token', FeatureActivation) >> CompletableFuture.supplyAsync {
                new FeatureActivation(
                    id: 27L,
                    enabled: true,
                    datetime: ZonedDateTime.now()
                )
            }
            0 * _
    }

    void "Get feature group activation returns Internal Server Error"() {
        when:
            sut.getFeatureGroupActivation('token', 'group-key', 27L).blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/activations/27", 'token', FeatureActivation) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Patch feature group activation returns OK"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(enabled: false)

        when:
            Single<FeatureActivation> result = sut.patchFeatureGroupActivation('token', 'group-key', 27L, newActivation)

        then:
            result.blockingGet().id == 27L

            1 * httpClient.patch("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/activations/27", 'token', newActivation, FeatureActivation) >> CompletableFuture.supplyAsync {
                new FeatureActivation(
                    id: 27L,
                    enabled: false,
                    datetime: ZonedDateTime.now()
                )
            }
            0 * _
    }

    void "Patch feature group activation returns Internal Server Error"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(enabled: false)

        when:
            sut.patchFeatureGroupActivation('token', 'group-key', 27L, newActivation).blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.patch("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/activations/27", 'token', newActivation, FeatureActivation) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Delete feature group activation returns OK"() {
        when:
            sut.deleteFeatureGroupActivation('token', 'group-key', 27L).blockingAwait()

        then:
            noExceptionThrown()

            1 * httpClient.delete("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/activations/27", 'token') >> CompletableFuture.completedFuture(null)
            0 * _
    }

    void "Patch feature group activation returns Internal Server Error"() {
        when:
            sut.deleteFeatureGroupActivation('token', 'group-key', 27L).blockingAwait()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.delete("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/activations/27", 'token') >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

}
