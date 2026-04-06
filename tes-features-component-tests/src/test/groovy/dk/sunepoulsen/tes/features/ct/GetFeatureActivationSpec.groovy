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

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Slf4j
class GetFeatureActivationSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingGet()
    }

    void "GET /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id} returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(registeredFeatureGroup).blockingGet()

        and: 'select a feature'
            Integer featureIndex = NumberGenerators.integerGenerator(0, registeredFeatureGroup.features.size()).generate()
            RegisterFeature registeredFeature = registeredFeatureGroup.features[featureIndex]

        and: 'add an extra activation'
            FeatureActivation newActivation = new FeatureActivation(
                enabled: false,
                datetime: ZonedDateTime.now(ZoneId.of('Z')).plusDays(1).truncatedTo(ChronoUnit.MICROS)
            )
            FeatureActivation createdActivation = featuresServiceIntegrator().features().activations().createFeatureActivation(registeredFeatureGroup.key, registeredFeature.key, newActivation).blockingGet()

        when: 'GET /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id}'
            FeatureActivation result = featuresServiceIntegrator().features().activations().getFeatureActivation(registeredFeatureGroup.key, registeredFeature.key, createdActivation.id).blockingGet()

        then: 'Verify response'
            result.id == createdActivation.id
            result.enabled == newActivation.enabled
            result.datetime == newActivation.datetime
    }

    @Unroll
    void "GET /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id} returns Bad request: #_testcase"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'GET /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id}'
            featuresServiceIntegrator().features().activations().getFeatureActivation(_featureGroupKey, _featureKey, 999).blockingGet()

        then: 'Verify response'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.response.statusCode() == 400
            exception.serviceError == new ServiceValidationErrorModel()

        where:
            _testcase                   | _featureGroupKey | _featureKey
            'Invalid feature group key' | 'wrong;key'      | 'valid-key'
            'Invalid feature key'       | 'valid-key'      | 'wrong;key'
    }

    void "GET /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id} returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(registeredFeatureGroup).blockingGet()

        and: 'select a feature'
            RegisterFeature registeredFeature = registeredFeatureGroup.features[0]

        when: 'GET /groups/{feature_group_key}/features/{feature_key}/activations/{activation_id} with non-existing activation id'
            featuresServiceIntegrator().features().activations().getFeatureActivation(registeredFeatureGroup.key, registeredFeature.key, 999).blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError == new ServiceErrorModel(
                message: 'No activation exists with the given keys and id: 999'
            )
    }

}
