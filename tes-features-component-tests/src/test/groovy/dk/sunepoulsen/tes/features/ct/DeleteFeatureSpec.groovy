package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.data.generators.NumberGenerators
import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesMockUsers
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.RegisterFeature
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientUnauthorizedException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
class DeleteFeatureSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider, FeaturesMockUsers {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "DELETE /groups/{feature_group_key}/features/{feature_key} returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and: 'register feature group and all its features'
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), registeredFeatureGroup).blockingGet()

        and: 'select a feature to delete'
            Integer featureIndex = NumberGenerators.integerGenerator(0, registeredFeatureGroup.features.size()).generate()
            RegisterFeature registeredFeature = registeredFeatureGroup.features[featureIndex]

        when: 'DELETE /groups/{feature_group_key}/features/{feature_key}'
            Void result = featuresServiceIntegrator().features().deleteFeature(featuresDefaultUser(), registeredFeatureGroup.key, registeredFeature.key).blockingAwait()

        then: 'Verify response'
            result == null
    }

    @Unroll
    void "DELETE /groups/{feature_group_key}/features/{feature_key} returns Bad request: #_testcase"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'DELETE /groups/{feature_group_key}/features/{feature_key}'
            featuresServiceIntegrator().features().deleteFeature(featuresDefaultUser(), _featureGroupKey, _featureKey).blockingAwait()

        then: 'Verify response'
            ClientUnauthorizedException exception = thrown(ClientUnauthorizedException)
            exception.response.statusCode() == 401
            exception.serviceError == new ServiceErrorModel(
                message: 'Service returned response with status 401'
            )

        where:
            _testcase                   | _featureGroupKey | _featureKey
            'Invalid feature group key' | 'wrong;key'      | 'valid-key'
            'Invalid feature key'       | 'valid-key'      | 'wrong;key'
    }

    void "DELETE /groups/{feature_group_key}/features/{feature_key} returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'DELETE /groups/{feature_group_key}/features/{feature_key}'
            featuresServiceIntegrator().features().deleteFeature(featuresDefaultUser(), 'some-key', 'some-feature-key').blockingAwait()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError == new ServiceErrorModel(
                message: "No feature with feature group 'some-key' and feature 'some-feature-key' exists"
            )
    }

}
