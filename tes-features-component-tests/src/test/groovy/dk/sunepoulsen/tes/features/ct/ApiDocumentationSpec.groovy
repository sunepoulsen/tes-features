package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.features.deployment.FeaturesIntegratorProvider
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ApiDocumentationSpec extends Specification implements FeaturesIntegratorProvider {

    private static List<String> DEFAULT_TAGS = ['Features']

    @Shared
    private apiDocResult

    void setupSpec() {
        isFeaturesServiceAvailable()
        this.apiDocResult = featuresIntegrator().apiDocumentation().blockingGet()
    }

    @Unroll
    void "#_method #_endpoint has api documentation"() {
        expect: 'Verify endpoint'
            apiDocResult.paths."${_endpoint}"."${_method.toLowerCase()}".tags == DEFAULT_TAGS

        where:
            _method | _endpoint
            'PUT'   | '/features'
    }

}
