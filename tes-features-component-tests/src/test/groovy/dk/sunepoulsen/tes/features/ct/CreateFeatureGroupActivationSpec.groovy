package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.FeatureActivation
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
class CreateFeatureGroupActivationSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingGet()
    }

    void "POST /groups/{feature_group_key}/activations returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and:
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(registeredFeatureGroup).blockingGet()

        when: 'has new activation body'
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true,
                datetime: ZonedDateTime.now(ZoneId.of('Z')).plusDays(1)
            )

        and: 'POST /groups/{feature_group_key}/activations'
            FeatureActivation result = featuresServiceIntegrator().featureGroups().activations().createFeatureGroupActivation(registeredFeatureGroup.key, newActivation).blockingGet()

        then: 'Verify response'
            result.id > 0
            result.enabled == newActivation.enabled
            result.datetime == newActivation.datetime
    }

    void "POST /groups/{feature_group_key}/activations returns bad request"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and:
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(registeredFeatureGroup).blockingGet()

        when: 'has new activation body'
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true
            )

        and: 'POST /groups/{feature_group_key}/activations'
            featuresServiceIntegrator().featureGroups().activations().createFeatureGroupActivation(registeredFeatureGroup.key, newActivation).blockingGet()

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

    void "POST /groups/{feature_group_key}/activations returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and:
            featuresServiceIntegrator().features().registerFeatures(registeredFeatureGroup).blockingGet()

        when: 'has new activation body'
            FeatureActivation newActivation = new FeatureActivation(
                enabled: true,
                datetime: ZonedDateTime.now().plusDays(1)
            )

        and: 'POST /groups/{feature_group_key}/activations'
            featuresServiceIntegrator().featureGroups().activations().createFeatureGroupActivation('some-key', newActivation).blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError.param == 'feature_group_key'
            exception.serviceError.message == "No feature group with key 'some-key' exists"
    }

}
