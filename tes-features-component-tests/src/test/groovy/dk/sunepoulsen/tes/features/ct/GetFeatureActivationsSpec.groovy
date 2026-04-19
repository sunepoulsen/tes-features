package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.data.generators.NumberGenerators
import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.EnvelopeFeatureActivation
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.features.model.RegisterFeature
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Unroll

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Slf4j
class GetFeatureActivationsSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "GET /groups/{feature_group_key}/features/{feature_key}/activations returns OK"() {
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
            featuresServiceIntegrator().features().activations().createFeatureActivation(registeredFeatureGroup.key, registeredFeature.key, newActivation).blockingGet()

        when: 'GET /groups/{feature_group_key}/features/{feature_key}/activations'
            EnvelopeFeatureActivation result = featuresServiceIntegrator().features().activations().getFeatureActivations(registeredFeatureGroup.key, registeredFeature.key).blockingGet()

        then: 'Verify response'
            result.results.size() == registeredFeature.activations.size() + 1
            result.results.any { it.enabled == true }
            result.results.any { it.enabled == false && it.datetime == newActivation.datetime }
    }

    @Unroll
    void "GET /groups/{feature_group_key}/features/{feature_key}/activations returns Bad request: #_testcase"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'GET /groups/{feature_group_key}/features/{feature_key}/activations'
            featuresServiceIntegrator().features().activations().getFeatureActivations(_featureGroupKey, _featureKey).blockingGet()

        then: 'Verify response'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.response.statusCode() == 400
            exception.serviceError == new ServiceValidationErrorModel()

        where:
            _testcase                   | _featureGroupKey | _featureKey
            'Invalid feature group key' | 'wrong;key'      | 'valid-key'
            'Invalid feature key'       | 'valid-key'      | 'wrong;key'
    }

    void "GET /groups/{feature_group_key}/features/{feature_key}/activations returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'GET /groups/{feature_group_key}/features/{feature_key}/activations with non-existing feature group key'
            featuresServiceIntegrator().features().activations().getFeatureActivations('non-existing-key', 'some-feature').blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
    }

}
