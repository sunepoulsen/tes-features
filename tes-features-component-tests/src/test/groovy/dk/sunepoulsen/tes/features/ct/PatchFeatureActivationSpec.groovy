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
import dk.sunepoulsen.tes.rest.models.ServiceValidationError
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class PatchFeatureActivationSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingGet()
    }

    void "PATCH /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id} returns OK"() {
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

        when: 'PATCH /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id}'
            FeatureActivation result = featuresServiceIntegrator().features().activations().patchFeatureActivation(registeredFeatureGroup.key, registeredFeature.key, featureActivation.id, new FeatureActivation(
                enabled: !featureActivation.enabled
            )).blockingGet()

        then: 'Verify response'
            result.id == featureActivation.id
            result.enabled == !featureActivation.enabled
            result.datetime == featureActivation.datetime
    }

    void "PATCH /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id} returns Bad Request"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'PATCH /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id}'
            featuresServiceIntegrator().features().activations().patchFeatureActivation('feature-group-key', 'feature-key', 999, new FeatureActivation(
                id: 37L
            )).blockingGet()

        then: 'Verify response'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.response.statusCode() == 400
            exception.serviceError == new ServiceValidationErrorModel(
                message: 'Unable to process request because of validation errors',
                validationErrors: [
                    new ServiceValidationError(
                        param: 'id',
                        message: 'must be null'
                    )
                ]
            )
    }

    void "PATCH /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id} returns not found"() {
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

        when: 'PATCH /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id}'
            featuresServiceIntegrator().features().activations().patchFeatureActivation(registeredFeatureGroup.key, registeredFeature.key, 999, new FeatureActivation(
                enabled: !featureActivation.enabled
            )).blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError == new ServiceErrorModel(
                message: "No activation with id '999' in feature group '${registeredFeatureGroup.key}' and feature '${registeredFeature.key}' exists"
            )
    }

}
