package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ApiDocumentationSpec extends Specification implements FeaturesServiceIntegratorProvider {

    private static List<String> FEATURE_TAG = ['Features']
    private static List<String> FEATURE_GROUP_TAG = ['Feature Groups']
    private static List<String> FEATURE_GROUP_ACTIVATIONS_TAG = ['Feature Group Activations']
    private static List<String> FEATURE_ACTIVATIONS_TAG = ['Feature Activations']

    @Shared
    private apiDocResult

    void setupSpec() {
        isFeaturesServiceAvailable()
        this.apiDocResult = featuresServiceIntegrator().apiDocumentation().blockingGet()
    }

    @Unroll
    void "#_method #_endpoint has api documentation"() {
        expect: 'Verify endpoint'
            apiDocResult.paths."${_endpoint}"."${_method.toLowerCase()}".tags == _tag

        where:
            _method  | _endpoint                                                                        | _tag
            'PUT'    | '/features'                                                                      | FEATURE_TAG
            'GET'    | '/groups'                                                                        | FEATURE_GROUP_TAG
            'GET'    | '/groups/{feature_group_key}'                                                    | FEATURE_GROUP_TAG
            'PATCH'  | '/groups/{feature_group_key}'                                                    | FEATURE_GROUP_TAG
            'DELETE' | '/groups/{feature_group_key}'                                                    | FEATURE_GROUP_TAG
            'POST'  | '/groups/{feature_group_key}/activations'                                        | FEATURE_GROUP_ACTIVATIONS_TAG
            'GET'   | '/groups/{feature_group_key}/activations'                                        | FEATURE_GROUP_ACTIVATIONS_TAG
            'GET'   | '/groups/{feature_group_key}/activations/{activation_id}'                        | FEATURE_GROUP_ACTIVATIONS_TAG
            'PATCH' | '/groups/{feature_group_key}/activations/{activation_id}'                        | FEATURE_GROUP_ACTIVATIONS_TAG
            'GET'    | '/groups/{feature_group_key}/features'                                           | FEATURE_TAG
            'GET'    | '/groups/{feature_group_key}/features/{feature_key}'                             | FEATURE_TAG
            'PATCH'  | '/groups/{feature_group_key}/features/{feature_key}'                             | FEATURE_TAG
            'DELETE' | '/groups/{feature_group_key}/features/{feature_key}'                             | FEATURE_TAG
            'POST'  | '/groups/{feature_group_key}/features/{feature_key}/activations'                 | FEATURE_ACTIVATIONS_TAG
            'GET'   | '/groups/{feature_group_key}/features/{feature_key}/activations'                 | FEATURE_ACTIVATIONS_TAG
            'GET'   | '/groups/{feature_group_key}/features/{feature_key}/activations/{activation_id}' | FEATURE_ACTIVATIONS_TAG
            'PATCH' | '/groups/{feature_group_key}/features/{feature_key}/activations/{activation_id}' | FEATURE_ACTIVATIONS_TAG
    }

}
