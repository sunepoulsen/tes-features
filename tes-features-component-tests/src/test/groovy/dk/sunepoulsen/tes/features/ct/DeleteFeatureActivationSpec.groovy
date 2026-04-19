package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.data.generators.NumberGenerators
import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.features.model.RegisterFeature
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
class DeleteFeatureActivationSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "DELETE /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id} returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(registeredFeatureGroup).blockingGet()

        and: 'select a feature to patch an activation for'
            Integer featureIndex = NumberGenerators.integerGenerator(0, registeredFeatureGroup.features.size()).generate()
            RegisterFeature registeredFeature = registeredFeatureGroup.features[featureIndex]

        and: 'select a feature group activation to patch'
            Integer activationIndex = NumberGenerators.integerGenerator(0, registeredFeature.activations.size()).generate()
            FeatureActivation featureActivation = registeredFeature.activations[activationIndex]

        when: 'DELETE /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id}'
            Void result = featuresServiceIntegrator().features().activations().deleteFeatureActivation(registeredFeatureGroup.key, registeredFeature.key, featureActivation.id).blockingAwait()

        then: 'Verify response'
            result == null
    }

    @Unroll
    void "DELETE /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id} returns Bad request: #_testcase"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'DELETE /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id}'
            featuresServiceIntegrator().features().activations().deleteFeatureActivation(_featureGroupKey, _featureKey, Long.MAX_VALUE).blockingAwait()

        then: 'Verify response'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.response.statusCode() == 400
            exception.serviceError == new ServiceValidationErrorModel()

        where:
            _testcase                   | _featureGroupKey | _featureKey
            'Invalid feature group key' | 'wrong;key'      | 'valid-key'
            'Invalid feature key'       | 'valid-key'      | 'wrong;key'
    }

    void "DELETE /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id} returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(registeredFeatureGroup).blockingGet()

        and: 'select a feature to patch an activation for'
            Integer featureIndex = NumberGenerators.integerGenerator(0, registeredFeatureGroup.features.size()).generate()
            RegisterFeature registeredFeature = registeredFeatureGroup.features[featureIndex]

        when: 'DELETE /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id}'
            featuresServiceIntegrator().features().activations().deleteFeatureActivation(registeredFeatureGroup.key, registeredFeature.key, Long.MAX_VALUE).blockingAwait()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError == new ServiceErrorModel(
                message: "No activation with id '${Long.MAX_VALUE}' in feature group '${registeredFeatureGroup.key}' and feature '${registeredFeature.key}' exists"
            )
    }

}
