package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesMockUsers
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.EnvelopeFeature
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientUnauthorizedException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class GetFeaturesServiceSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider, FeaturesMockUsers {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "GET /groups/{feature_group_key}/features returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            List<RegisterFeatureGroup> featureGroups = [
                new RegisterFeatureGroupDataGenerator().generate(),
                new RegisterFeatureGroupDataGenerator().generate(),
                new RegisterFeatureGroupDataGenerator().generate()
            ]

        and:
            featureGroups.each {
                featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), it).blockingGet()
            }

        when: 'GET /groups/{feature_group_key}/features'
            EnvelopeFeature envelopeFeature = featuresServiceIntegrator().features().getFeatures(featuresDefaultUser(), featureGroups[1].key).blockingGet()

        then: 'Verify response'
            envelopeFeature.results.size() == featureGroups[1].features.size()
            (0..envelopeFeature.results.size() - 1).each {
                assert envelopeFeature.results[it].key == featureGroups[1].features[it].key
                assert envelopeFeature.results[it].name == featureGroups[1].features[it].name
                assert envelopeFeature.results[it].description == featureGroups[1].features[it].description
            }
    }

    void "GET /groups/{feature_group_key}/features returns Bad Request"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'Call GET /groups/{feature_group_key}/features'
            featuresServiceIntegrator().features().getFeatures(featuresDefaultUser(), 'wrong;key').blockingGet()

        then: 'Verify response'
            ClientUnauthorizedException exception = thrown(ClientUnauthorizedException)
            exception.response.statusCode() == 401
            exception.serviceError == new ServiceErrorModel(
                message: 'Service returned response with status 401'
            )
    }

    void "GET /groups/{feature_group_key}/features returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'GET /groups/{feature_group_key}/features'
            featuresServiceIntegrator().features().getFeatures(featuresDefaultUser(), 'group-key').blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError == new ServiceErrorModel(
                param: 'feature_group_key',
                message: "No feature group with key 'group-key' exists"
            )
    }

}
