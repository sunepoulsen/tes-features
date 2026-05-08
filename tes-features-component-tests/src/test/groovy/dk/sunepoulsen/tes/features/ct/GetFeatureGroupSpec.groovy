package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesMockUsers
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.FeatureGroup
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientUnauthorizedException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class GetFeatureGroupSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider, FeaturesMockUsers {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "GET /groups/{feature_group_key} returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup featureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and:
            featureGroup = featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), featureGroup).blockingGet()

        when: 'GET /groups/{feature_group_key}'
            FeatureGroup result = featuresServiceIntegrator().featureGroups().getFeatureGroup(featuresDefaultUser(), featureGroup.key).blockingGet()

        then: 'Verify response'
            with(result) {
                assert it.key == featureGroup.key
                assert it.name == featureGroup.name
                assert it.description == featureGroup.description
            }
    }

    void "GET /groups/{feature_group_key} returns Bad Request"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'Call GET /groups/{feature_group_key}'
            featuresServiceIntegrator().featureGroups().getFeatureGroup(featuresDefaultUser(), 'wrong;key').blockingGet()

        then: 'Verify response'
            ClientUnauthorizedException exception = thrown(ClientUnauthorizedException)
            exception.response.statusCode() == 401
            exception.serviceError == new ServiceErrorModel(
                message: 'Service returned response with status 401'
            )
    }

    void "GET /groups/{feature_group_key} returns Not Found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'Call GET /groups/{feature_group_key}'
            featuresServiceIntegrator().featureGroups().getFeatureGroup(featuresDefaultUser(), 'wrong-key').blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError == new ServiceErrorModel(
                param: 'key',
                message: 'No feature group exists with key: wrong-key'
            )
    }

}
