package dk.sunepoulsen.tes.features.ct


import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.EnvelopeFeature
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class GetFeaturesSpec extends Specification implements FeaturesIntegratorProvider, FeaturesTestsIntegratorProvider {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingGet()
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
                featuresIntegrator().registerFeatures(it).blockingGet()
            }

        when: 'GET /groups/{feature_group_key}/features'
            EnvelopeFeature envelopeFeature = featuresIntegrator().getFeatures(featureGroups[1].key).blockingGet()

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
            featuresIntegrator().getFeatures('wrong;key').blockingGet()

        then: 'Verify response'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.response.statusCode() == 400
            exception.serviceError == new ServiceValidationErrorModel()
    }

    void "GET /groups/{feature_group_key}/features returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'GET /groups/{feature_group_key}/features'
            featuresIntegrator().getFeatures('group-key').blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError == new ServiceErrorModel(
                param: 'feature_group_key',
                message: "No feature group with key 'group-key' exists"
            )
    }

}
