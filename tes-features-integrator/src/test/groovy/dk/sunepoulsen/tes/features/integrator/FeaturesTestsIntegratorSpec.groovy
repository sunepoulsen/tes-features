package dk.sunepoulsen.tes.features.integrator

import dk.sunepoulsen.tes.rest.integrations.TechEasySolutionsClient
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientResponseException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import spock.lang.Specification

import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class FeaturesTestsIntegratorSpec extends Specification {

    private TechEasySolutionsClient httpClient
    private FeaturesTestsIntegrator sut

    void setup() {
        this.httpClient = Mock(TechEasySolutionsClient)
        this.sut = new FeaturesTestsIntegrator(this.httpClient)
    }

    void "DELETE persistence with OK"() {
        when:
            this.sut.deletePersistence().blockingGet()

        then:
            1 * httpClient.delete('/tests/persistence') >> CompletableFuture.supplyAsync { "" }
    }

    void "POST DELETE persistence with ForbiddenRequest result"() {
        when:
            this.sut.deletePersistence().blockingGet()

        then:
            1 * httpClient.delete('/tests/persistence') >> CompletableFuture.supplyAsync {
                throw new ExecutionException("message", new ClientResponseException(Mock(HttpResponse), new ServiceErrorModel(
                    code: 'code',
                    param: 'param',
                    message: 'message'
                )))
            }
            ClientResponseException ex = thrown(ClientResponseException)
            ex.serviceError.code == 'code'
            ex.serviceError.param == 'param'
            ex.serviceError.message == 'message'
    }

}
