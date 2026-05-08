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

class FeatureActivationsIntegratorSpec extends Specification {

    private TechEasySolutionsClient httpClient
    private FeatureActivationsIntegrator sut

    void setup() {
        this.httpClient = Mock(TechEasySolutionsClient)
        this.sut = new FeatureActivationsIntegrator(this.httpClient)
    }

    void "Create new activation for a feature with OK"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true,
                datetime: ZonedDateTime.now()
            )

        when:
            Single<FeatureActivation> result = sut.createFeatureActivation('token', 'group-key', 'feature-key', newActivation)

        then:
            result.blockingGet().id == 27L

            1 * httpClient.post("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations", 'token', newActivation, FeatureActivation) >> CompletableFuture.supplyAsync {
                new FeatureActivation(
                    id: 27L,
                    enabled: true,
                    datetime: ZonedDateTime.now()
                )
            }
            0 * _
    }

    void "Create new activation for a feature with Internal Server Error"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true,
                datetime: ZonedDateTime.now()
            )

        when:
            sut.createFeatureActivation('token', 'group-key', 'feature-key', newActivation).blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.post("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations", 'token', newActivation, FeatureActivation) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Get feature activations returns OK"() {
        when:
            Single<EnvelopeFeatureActivation> result = sut.getFeatureActivations('token', 'group-key', 'feature-key')

        then:
            result.blockingGet().results.first.id == 27L

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations", 'token', EnvelopeFeatureActivation) >> CompletableFuture.supplyAsync {
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

    void "Get feature activations returns Internal Server Error"() {
        when:
            sut.getFeatureActivations('token', 'group-key', 'feature-key').blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations", 'token', EnvelopeFeatureActivation) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Get feature activation returns OK"() {
        when:
            Single<FeatureActivation> result = sut.getFeatureActivation('token', 'group-key', 'feature-key', 27L)

        then:
            result.blockingGet().id == 27L

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations/27", 'token', FeatureActivation) >> CompletableFuture.supplyAsync {
                new FeatureActivation(
                    id: 27L,
                    enabled: true,
                    datetime: ZonedDateTime.now()
                )
            }
            0 * _
    }

    void "Get feature activation returns Internal Server Error"() {
        when:
            sut.getFeatureActivation('token', 'group-key', 'feature-key', 27L).blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations/27", 'token', FeatureActivation) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Patch feature activation returns OK"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(enabled: false)

        when:
            Single<FeatureActivation> result = sut.patchFeatureActivation('token', 'group-key', 'feature-key', 27L, newActivation)

        then:
            result.blockingGet().id == 27L

            1 * httpClient.patch("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations/27", 'token', newActivation, FeatureActivation) >> CompletableFuture.supplyAsync {
                new FeatureActivation(
                    id: 27L,
                    enabled: false,
                    datetime: ZonedDateTime.now()
                )
            }
            0 * _
    }

    void "Patch feature activation returns Internal Server Error"() {
        given:
            FeatureActivation newActivation = new FeatureActivation(enabled: false)

        when:
            sut.patchFeatureActivation('token', 'group-key', 'feature-key', 27L, newActivation).blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.patch("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations/27", 'token', newActivation, FeatureActivation) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Patch feature activation returns OK"() {
        when:
            sut.deleteFeatureActivation('token', 'group-key', 'feature-key', 27L).blockingAwait()

        then:
            noExceptionThrown()

            1 * httpClient.delete("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations/27", 'token') >> CompletableFuture.completedFuture(null)
            0 * _
    }

    void "Delete feature activation returns Internal Server Error"() {
        when:
            sut.deleteFeatureActivation('token', 'group-key', 'feature-key', 27L).blockingAwait()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.delete("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations/27", 'token') >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

}
