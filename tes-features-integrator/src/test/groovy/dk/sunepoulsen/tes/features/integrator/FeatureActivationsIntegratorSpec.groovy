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
            Single<FeatureActivation> result = sut.createFeatureActivation('group-key', 'feature-key', newActivation)

        then:
            result.blockingGet().id == 27L

            1 * httpClient.post("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations", newActivation, FeatureActivation) >> CompletableFuture.supplyAsync {
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
            sut.createFeatureActivation('group-key', 'feature-key', newActivation).blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.post("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations", newActivation, FeatureActivation) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Get feature activations returns OK"() {
        when:
            Single<EnvelopeFeatureActivation> result = sut.getFeatureActivations('group-key', 'feature-key')

        then:
            result.blockingGet().results.first.id == 27L

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations", EnvelopeFeatureActivation) >> CompletableFuture.supplyAsync {
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
            sut.getFeatureActivations('group-key', 'feature-key').blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations", EnvelopeFeatureActivation) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Get feature activation returns OK"() {
        when:
            Single<FeatureActivation> result = sut.getFeatureActivation('group-key', 'feature-key', 27L)

        then:
            result.blockingGet().id == 27L

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations/27", FeatureActivation) >> CompletableFuture.supplyAsync {
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
            sut.getFeatureActivation('group-key', 'feature-key', 27L).blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.get("${FeatureGroupsIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key/features/feature-key/activations/27", FeatureActivation) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

}
