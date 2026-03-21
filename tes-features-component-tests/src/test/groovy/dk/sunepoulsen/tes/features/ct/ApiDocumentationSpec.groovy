package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.features.deployment.FeaturesIntegratorProvider
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ApiDocumentationSpec extends Specification implements FeaturesIntegratorProvider {

    private static List<String> FEATURE_TAG = ['Features']
    private static List<String> FEATURE_GROUP_TAG = ['Feature Groups']

    @Shared
    private apiDocResult

    void setupSpec() {
        isFeaturesServiceAvailable()
        this.apiDocResult = featuresIntegrator().apiDocumentation().blockingGet()
    }

    @Unroll
    void "#_method #_endpoint has api documentation"() {
        expect: 'Verify endpoint'
            apiDocResult.paths."${_endpoint}"."${_method.toLowerCase()}".tags == _tag

        where:
            _method | _endpoint                     | _tag
            'PUT'   | '/features'                   | FEATURE_TAG
            'GET'   | '/groups/{feature_group_key}' | FEATURE_GROUP_TAG
    }

}
