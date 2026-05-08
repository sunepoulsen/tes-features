package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesMockUsers
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientUnauthorizedException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Slf4j
class GetFeatureGroupActivationSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider, FeaturesMockUsers {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "GET /groups/{feature_group_key}/activations/{activation_id} returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), registeredFeatureGroup).blockingGet()

        and: 'add an extra activation'
            FeatureActivation newActivation = new FeatureActivation(
                enabled: false,
                datetime: ZonedDateTime.now(ZoneId.of('Z')).plusDays(1).truncatedTo(ChronoUnit.MICROS)
            )
            FeatureActivation createdActivation = featuresServiceIntegrator().featureGroups().activations().createFeatureGroupActivation(featuresDefaultUser(), registeredFeatureGroup.key, newActivation).blockingGet()

        when: 'GET /groups/{feature_group_key}/activations/{activation_id}'
            FeatureActivation result = featuresServiceIntegrator().featureGroups().activations().getFeatureGroupActivation(featuresDefaultUser(), registeredFeatureGroup.key, createdActivation.id).blockingGet()

        then: 'Verify response'
            result.id == createdActivation.id
            result.enabled == newActivation.enabled
            result.datetime == newActivation.datetime
    }

    void "GET /groups/{feature_group_key}/activations/{activation_id} returns Bad Request"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'Call GET /groups/{feature_group_key}/activations/{activation_id}'
            featuresServiceIntegrator().featureGroups().activations().getFeatureGroupActivation(featuresDefaultUser(), 'wrong;key', 999).blockingGet()

        then: 'Verify response'
            ClientUnauthorizedException exception = thrown(ClientUnauthorizedException)
            exception.response.statusCode() == 401
            exception.serviceError == new ServiceErrorModel(
                message: 'Service returned response with status 401'
            )
    }

    void "GET /groups/{feature_group_key}/activations/{activation_id} returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), registeredFeatureGroup).blockingGet()

        when: 'GET /groups/{feature_group_key}/activations/{activation_id} with non-existing activation id'
            featuresServiceIntegrator().featureGroups().activations().getFeatureGroupActivation(featuresDefaultUser(), registeredFeatureGroup.key, 999).blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError == new ServiceErrorModel(
                param: 'activation_id',
                message: 'No activation exists with id: 999'
            )
    }

}
