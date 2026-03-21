package dk.sunepoulsen.tes.features.integrator

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureGroup
import dk.sunepoulsen.tes.features.model.FeatureGroup
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientInternalServerException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import io.reactivex.rxjava3.core.Single
import spock.lang.Specification

import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class FeaturesIntegratorSpec extends Specification {

    private TechEasySolutionsClient httpClient
    private FeaturesIntegrator sut

    void setup() {
        this.httpClient = Mock(TechEasySolutionsClient)
        this.sut = new FeaturesIntegrator(this.httpClient)
    }

    void "Register features with OK"() {
        given:
            RegisterFeatureGroup model = new RegisterFeatureGroup()

        when:
            Single<RegisterFeatureGroup> result = sut.registerFeatures(model)

        then:
            result.blockingGet().key == 'group-key'

            1 * httpClient.put(FeaturesIntegrator.FEATURE_ENDPOINT_PATH, model, RegisterFeatureGroup) >> CompletableFuture.supplyAsync {
                new RegisterFeatureGroup(
                    key: 'group-key'
                )
            }
    }

    void "Register features with Internal Server Error"() {
        given:
            RegisterFeatureGroup model = new RegisterFeatureGroup()

        when:
            sut.registerFeatures(model).blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.put(FeaturesIntegrator.FEATURE_ENDPOINT_PATH, model, RegisterFeatureGroup) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Get feature groups with OK"() {
        when:
            Single<EnvelopeFeatureGroup> result = sut.getFeatureGroups()

        then:
            result.blockingGet().results.first.key == 'group-key'

            1 * httpClient.get("${FeaturesIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}", EnvelopeFeatureGroup) >> CompletableFuture.supplyAsync {
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
            sut.getFeatureGroups().blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.get("${FeaturesIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}", EnvelopeFeatureGroup) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Get feature group with OK"() {
        when:
            Single<FeatureGroup> result = sut.getFeatureGroup('group-key')

        then:
            result.blockingGet().key == 'group-key'

            1 * httpClient.get("${FeaturesIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key", FeatureGroup) >> CompletableFuture.supplyAsync {
                new FeatureGroup(
                    key: 'group-key'
                )
            }
            0 * _
    }

    void "Get feature group Internal Server Error"() {
        when:
            sut.getFeatureGroup('group-key').blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.get("${FeaturesIntegrator.FEATURE_GROUPS_ENDPOINT_PATH}/group-key", FeatureGroup) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

}
