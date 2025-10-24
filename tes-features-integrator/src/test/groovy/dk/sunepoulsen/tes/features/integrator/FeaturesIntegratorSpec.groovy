package dk.sunepoulsen.tes.features.integrator

import dk.sunepoulsen.tes.features.model.FeatureGroup
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
            FeatureGroup model = new FeatureGroup()

        when:
            Single<FeatureGroup> result = sut.registerFeatures(model)

        then:
            result.blockingGet().key == 'group-key'

            1 * httpClient.put('/features', model, FeatureGroup) >> CompletableFuture.supplyAsync {
                new FeatureGroup(
                    key: 'group-key'
                )
            }
    }

    void "Register features with Internal Server Error"() {
        given:
            FeatureGroup model = new FeatureGroup()

        when:
            sut.registerFeatures(model).blockingGet()

        then:
            ClientInternalServerException ex = thrown(ClientInternalServerException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'

            1 * httpClient.put('/features', model, FeatureGroup) >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientInternalServerException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
    }

}
