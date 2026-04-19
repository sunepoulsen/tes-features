package dk.sunepoulsen.tes.features.ct


import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.EnvelopeFeatureActivation
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Slf4j
class GetFeatureGroupActivationsSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "GET /groups/{feature_group_key}/activations returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(registeredFeatureGroup).blockingGet()

        and: 'add an extra activation'
            FeatureActivation newActivation = new FeatureActivation(
                enabled: false,
                datetime: ZonedDateTime.now(ZoneId.of('Z')).plusDays(1).truncatedTo(ChronoUnit.MICROS)
            )
            featuresServiceIntegrator().featureGroups().activations().createFeatureGroupActivation(registeredFeatureGroup.key, newActivation).blockingGet()

        when: 'GET /groups/{feature_group_key}/activations'
            EnvelopeFeatureActivation result = featuresServiceIntegrator().featureGroups().activations().getFeatureGroupActivations(registeredFeatureGroup.key).blockingGet()

        then: 'Verify response'
            result.results.size() == registeredFeatureGroup.activations.size() + 1
            result.results.any { it.enabled == true }
            result.results.any { it.enabled == false && it.datetime == newActivation.datetime }
    }

    void "GET /groups/{feature_group_key}/activations returns Bad Request"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'Call /groups/{feature_group_key}/activations'
            featuresServiceIntegrator().featureGroups().activations().getFeatureGroupActivations('wrong;key').blockingGet()

        then: 'Verify response'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.response.statusCode() == 400
            exception.serviceError == new ServiceValidationErrorModel()
    }

    void "GET /groups/{feature_group_key}/activations returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'GET /groups/{feature_group_key}/activations with non-existing key'
            featuresServiceIntegrator().featureGroups().activations().getFeatureGroupActivations('non-existing-key').blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
    }

}
