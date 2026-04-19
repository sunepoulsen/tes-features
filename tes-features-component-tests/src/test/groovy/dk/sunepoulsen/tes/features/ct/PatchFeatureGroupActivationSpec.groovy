package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.data.generators.NumberGenerators
import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import dk.sunepoulsen.tes.rest.models.ServiceValidationError
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class PatchFeatureGroupActivationSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "PATCH /groups/{feature_group_key}/activations/{activation_id} returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(registeredFeatureGroup).blockingGet()

        and: 'select an feature group activation to patch'
            Integer activationIndex = NumberGenerators.integerGenerator(0, registeredFeatureGroup.activations.size()).generate()
            FeatureActivation featureActivation = registeredFeatureGroup.activations[activationIndex]

        when: 'PATCH /groups/{feature_group_key}/activations/{activation_id}'
            FeatureActivation result = featuresServiceIntegrator().featureGroups().activations().patchFeatureGroupActivation(registeredFeatureGroup.key, featureActivation.id, new FeatureActivation(
                enabled: !featureActivation.enabled
            )).blockingGet()

        then: 'Verify response'
            result.id == featureActivation.id
            result.enabled == !featureActivation.enabled
            result.datetime == featureActivation.datetime
    }

    void "PATCH /groups/{feature_group_key}/activations/{activation_id} returns Bad Request"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'PATCH /groups/{feature_group_key}/activations/{activation_id}'
            featuresServiceIntegrator().featureGroups().activations().patchFeatureGroupActivation('feature-group-key', 999, new FeatureActivation(
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

    void "PATCH /groups/{feature_group_key}/activations/{activation_id} returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(registeredFeatureGroup).blockingGet()

        when: 'PATCH /groups/{feature_group_key}/activations/{activation_id}'
            FeatureActivation result = featuresServiceIntegrator().featureGroups().activations().patchFeatureGroupActivation(registeredFeatureGroup.key, 999, new FeatureActivation(
                enabled: false
            )).blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError == new ServiceErrorModel(
                message: "No activation with id '999' and feature group '${registeredFeatureGroup.key}' exists"
            )
    }

}
