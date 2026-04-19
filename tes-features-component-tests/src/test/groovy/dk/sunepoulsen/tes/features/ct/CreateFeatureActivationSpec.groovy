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
import dk.sunepoulsen.tes.rest.models.ServiceValidationError
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime

@Slf4j
class CreateFeatureActivationSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "POST /groups/{feature_group_key}/features/{feature_key}/activations returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and:
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(registeredFeatureGroup).blockingGet()

        and: 'select a feature to add a new activation for'
            Integer featureIndex = NumberGenerators.integerGenerator(0, registeredFeatureGroup.features.size()).generate()
            RegisterFeature registeredFeature = registeredFeatureGroup.features[featureIndex]

        when: 'has new activation body'
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true,
                datetime: ZonedDateTime.now(ZoneId.of('Z')).plusDays(1)
            )

        and: 'POST /groups/{feature_group_key}/features/{feature_key}/activations'
            FeatureActivation result = featuresServiceIntegrator().features().activations().createFeatureActivation(registeredFeatureGroup.key, registeredFeature.key, newActivation).blockingGet()

        then: 'Verify response'
            result.id > 0
            result.enabled == newActivation.enabled
            result.datetime == newActivation.datetime
    }

    void "POST /groups/{feature_group_key}/features/{feature_key}/activations returns bad request"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and:
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(registeredFeatureGroup).blockingGet()

        and: 'select a feature to add a new activation for'
            Integer featureIndex = NumberGenerators.integerGenerator(0, registeredFeatureGroup.features.size()).generate()
            RegisterFeature registeredFeature = registeredFeatureGroup.features[featureIndex]

        when: 'has new activation body'
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true
            )

        and: 'POST /groups/{feature_group_key}/features/{feature_key}/activations'
            featuresServiceIntegrator().features().activations().createFeatureActivation(registeredFeatureGroup.key, registeredFeature.key, newActivation).blockingGet()

        then: 'Verify response'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.response.statusCode() == 400
            exception.serviceError == new ServiceValidationErrorModel(
                message: 'Unable to process request because of validation errors',
                validationErrors: [
                    new ServiceValidationError(
                        param: 'datetime',
                        message: 'must not be null'
                    )
                ]
            )
    }

    void "POST /groups/{feature_group_key}/features/{feature_key}/activations returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and:
            featuresServiceIntegrator().features().registerFeatures(registeredFeatureGroup).blockingGet()

        and: 'select a feature to add a new activation for'
            Integer featureIndex = NumberGenerators.integerGenerator(0, registeredFeatureGroup.features.size()).generate()
            RegisterFeature registeredFeature = registeredFeatureGroup.features[featureIndex]

        when: 'has new activation body'
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true,
                datetime: ZonedDateTime.now().plusDays(1)
            )

        and: 'POST /groups/{feature_group_key}/features/{feature_key}/activations'
            featuresServiceIntegrator().features().activations().createFeatureActivation('some-key', registeredFeature.key, newActivation).blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError.message == "No feature with feature group 'some-key' and feature '${registeredFeature.key}' exists"
    }

}
