package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesMockUsers
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class DeleteFeatureGroupSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider, FeaturesMockUsers {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "DELETE /groups/{feature_group_key} returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and:
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), registeredFeatureGroup).blockingGet()

        when: 'DELETE /groups/{feature_group_key}'
            Void result = featuresServiceIntegrator().featureGroups().deleteFeatureGroup(featuresDefaultUser(), registeredFeatureGroup.key).blockingAwait()

        then: 'Verify response'
            result == null
    }

    void "DELETE /groups/{feature_group_key} returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'DELETE /groups/{feature_group_key}'
            featuresServiceIntegrator().featureGroups().deleteFeatureGroup(featuresDefaultUser(), 'some-key').blockingAwait()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError.param == 'feature_group_key'
            exception.serviceError.message == "No feature group with key 'some-key' exists"
    }

}
