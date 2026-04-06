package dk.sunepoulsen.tes.features.ct


import dk.sunepoulsen.tes.data.generators.NumberGenerators
import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.Feature
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Unroll

@Slf4j
class GetFeatureSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingGet()
    }

    void "GET /groups/{feature_group_key}/features/{feature_key} returns OK"() {
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
                featuresServiceIntegrator().features().registerFeatures(it).blockingGet()
            }

        and:
            RegisterFeatureGroup featureGroup = featureGroups[1]
            Integer featureIndex = NumberGenerators.integerGenerator(0, featureGroup.features.size()).generate()

        when: 'GET /groups/{feature_group_key}/features/{feature_key}'
            Feature responseFeature = featuresServiceIntegrator().features().getFeature(featureGroup.key, featureGroup.features[featureIndex].key).blockingGet()

        then: 'Verify response'
            assert responseFeature.key == featureGroup.features[featureIndex].key
            assert responseFeature.name == featureGroup.features[featureIndex].name
            assert responseFeature.description == featureGroup.features[featureIndex].description
    }

    @Unroll
    void "GET /groups/{feature_group_key}/features/{feature_key} returns Bad request: #_testcase"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'GET /groups/{feature_group_key}/features/{feature_key}'
            featuresServiceIntegrator().features().getFeature(_featureGroupKey, _featureKey).blockingGet()

        then: 'Verify response'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.response.statusCode() == 400
            exception.serviceError == new ServiceValidationErrorModel()

        where:
            _testcase                   | _featureGroupKey | _featureKey
            'Invalid feature group key' | 'wrong;key'      | 'valid-key'
            'Invalid feature key'       | 'valid-key'      | 'wrong;key'
    }

    void "GET /groups/{feature_group_key}/features/{feature_key} returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'GET /groups/{feature_group_key}/features/{feature_key}'
            featuresServiceIntegrator().features().getFeature('group-key', 'key').blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError == new ServiceErrorModel(
                message: "No feature exists with the given keys"
            )
    }

}
