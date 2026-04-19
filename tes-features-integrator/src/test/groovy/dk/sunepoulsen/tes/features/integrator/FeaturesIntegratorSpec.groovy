package dk.sunepoulsen.tes.features.integrator


import dk.sunepoulsen.tes.features.model.EnvelopeFeature
import dk.sunepoulsen.tes.features.model.Feature
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientInternalServerException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
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

            1 * httpClient.put(FeaturesIntegrator.REGISTER_FEATURES_ENDPOINT_PATH, model, RegisterFeatureGroup) >> CompletableFuture.supplyAsync {
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

            1 * httpClient.put(FeaturesIntegrator.REGISTER_FEATURES_ENDPOINT_PATH, model, RegisterFeatureGroup) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }


    void "Get features of feature group returns OK"() {
        when:
            Single<EnvelopeFeature> result = sut.getFeatures('group-key')

        then:
            result.blockingGet().results.first.key == 'feature-key'

            1 * httpClient.get(String.format(FeaturesIntegrator.FEATURES_ENDPOINT_PATH, 'group-key'), EnvelopeFeature) >> CompletableFuture.supplyAsync {
                new EnvelopeFeature(
                    results: [
                        new Feature(
                            key: 'feature-key'
                        )
                    ]
                )
            }
            0 * _
    }

    void "Get features of feature group returns Internal Server Error"() {
        when:
            sut.getFeatures('group-key').blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.get(String.format(FeaturesIntegrator.FEATURES_ENDPOINT_PATH, 'group-key'), EnvelopeFeature) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

    void "Get feature of feature group returns OK"() {
        when:
            Single<Feature> result = sut.getFeature('group-key', 'feature-key')

        then:
            result.blockingGet().key == 'feature-key'

            1 * httpClient.get(String.format(FeaturesIntegrator.FEATURE_ENDPOINT_PATH, 'group-key', 'feature-key'), Feature) >> CompletableFuture.supplyAsync {
                new Feature(
                    key: 'feature-key'
                )
            }
            0 * _
    }

    void "Get feature of feature group returns Internal Server Error"() {
        when:
            sut.getFeature('group-key', 'feature-key').blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.get(String.format(FeaturesIntegrator.FEATURE_ENDPOINT_PATH, 'group-key', 'feature-key'), Feature) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
            0 * _
    }

    void "Patch feature with OK"() {
        given:
            Feature newValues = new Feature(
                name: 'new-name'
            )

        when:
            Single<Feature> result = sut.patchFeature('group-key', 'key', newValues)

        then:
            result.blockingGet().key == 'key'

            1 * httpClient.patch(String.format(FeaturesIntegrator.FEATURE_ENDPOINT_PATH, 'group-key', 'key'), newValues, Feature) >> CompletableFuture.supplyAsync {
                new Feature(
                    key: 'key'
                )
            }
            0 * _
    }

    void "Patch feature that returns Internal Server Error"() {
        given:
            Feature newValues = new Feature(
                name: 'new-name'
            )

        when:
            sut.patchFeature('group-key', 'key', newValues).blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.patch(String.format(FeaturesIntegrator.FEATURE_ENDPOINT_PATH, 'group-key', 'key'), newValues, Feature) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
            0 * _
    }

    void "Delete feature from a feature group with OK"() {
        when:
            sut.deleteFeature('group-key', 'key').blockingAwait()

        then:
            noExceptionThrown()

            1 * httpClient.delete(String.format(FeaturesIntegrator.FEATURE_ENDPOINT_PATH, 'group-key', 'key')) >> CompletableFuture.completedFuture(null)
            0 * _
    }

    void "Delete feature from a feature group with not found"() {
        when:
            sut.deleteFeature('group-key', 'key').blockingAwait()

        then:
            ClientNotFoundException ex = thrown(ClientNotFoundException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.delete(String.format(FeaturesIntegrator.FEATURE_ENDPOINT_PATH, 'group-key', 'key')) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientNotFoundException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
            0 * _
    }

}
