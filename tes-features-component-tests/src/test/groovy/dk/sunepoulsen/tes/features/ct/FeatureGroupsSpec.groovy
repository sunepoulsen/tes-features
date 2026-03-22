package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.data.generators.CharacterGenerator
import dk.sunepoulsen.tes.data.generators.DataGenerator
import dk.sunepoulsen.tes.data.generators.Generators
import dk.sunepoulsen.tes.data.generators.NumberGenerators
import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.EnvelopeFeatureGroup
import dk.sunepoulsen.tes.features.model.FeatureGroup
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import dk.sunepoulsen.tes.rest.models.ServiceValidationError
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class FeatureGroupsSpec extends Specification implements FeaturesIntegratorProvider, FeaturesTestsIntegratorProvider {

    private DataGenerator<String> textGenerator

    void setup() {
        this.textGenerator = Generators.textGenerator(
            [CharacterGenerator.URI_PATH_CHARECTERS],
            NumberGenerators.integerGenerator(5, 50)
        )
        featuresTestsIntegrator().deletePersistence().blockingGet()
    }

    void "GET /groups returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            List<RegisterFeatureGroup> featureGroups = [
                new RegisterFeatureGroupDataGenerator(textGenerator).generate(),
                new RegisterFeatureGroupDataGenerator(textGenerator).generate(),
                new RegisterFeatureGroupDataGenerator(textGenerator).generate()
            ]

        and:
            featureGroups.each {
                featuresIntegrator().registerFeatures(it).blockingGet()
            }

        when: 'GET /groups'
            EnvelopeFeatureGroup result = featuresIntegrator().getFeatureGroups().blockingGet()

        then: 'Verify response'
            with(result) {
                assert it.results[0].key == featureGroups[0].key
                assert it.results[0].name == featureGroups[0].name
                assert it.results[0].description == featureGroups[0].description

                assert it.results[1].key == featureGroups[1].key
                assert it.results[1].name == featureGroups[1].name
                assert it.results[1].description == featureGroups[1].description

                assert it.results[2].key == featureGroups[2].key
                assert it.results[2].name == featureGroups[2].name
                assert it.results[2].description == featureGroups[2].description
            }
    }

    void "GET /groups/{feature_group_key} returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup featureGroup = new RegisterFeatureGroupDataGenerator(textGenerator).generate()

        and:
            featureGroup = featuresIntegrator().registerFeatures(featureGroup).blockingGet()

        when: 'GET /groups/{feature_group_key}'
            FeatureGroup result = featuresIntegrator().getFeatureGroup(featureGroup.key).blockingGet()

        then: 'Verify response'
            with(result) {
                assert it.key == featureGroup.key
                assert it.name == featureGroup.name
                assert it.description == featureGroup.description
            }
    }

    void "GET /groups/{feature_group_key} returns Bad Request"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'Call GET /groups/{feature_group_key}'
            featuresIntegrator().getFeatureGroup('wrong;key').blockingGet()

        then: 'Verify response'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.response.statusCode() == 400
            exception.serviceError == new ServiceValidationErrorModel()
    }

    void "GET /groups/{feature_group_key} returns Not Found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'Call GET /groups/{feature_group_key}'
            featuresIntegrator().getFeatureGroup('wrong-key').blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError == new ServiceErrorModel(
                param: 'key',
                message: 'No feature group exists with key: wrong-key'
            )
    }

    void "PATCH /groups/{feature_group_key} returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator(textGenerator).generate()

        and:
            registeredFeatureGroup = featuresIntegrator().registerFeatures(registeredFeatureGroup).blockingGet()

        when: 'has valid patch body'
            FeatureGroup featureGroup = new FeatureGroup(
                name: 'new-name'
            )

        and: 'PATCH /groups/{feature_group_key}'
            FeatureGroup result = featuresIntegrator().patchFeatureGroup(registeredFeatureGroup.key, featureGroup).blockingGet()

        then: 'Verify response'
            with(result) {
                assert it.key == registeredFeatureGroup.key
                assert it.name == featureGroup.name
                assert it.description == registeredFeatureGroup.description
            }
    }

    void "PATCH /groups/{feature_group_key} returns bad request"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator(textGenerator).generate()

        and:
            registeredFeatureGroup = featuresIntegrator().registerFeatures(registeredFeatureGroup).blockingGet()

        when: 'has invalid patch body'
            FeatureGroup featureGroup = new FeatureGroup(
                key: 'not-null'
            )

        and: 'PATCH /groups/{feature_group_key}'
            featuresIntegrator().patchFeatureGroup(registeredFeatureGroup.key, featureGroup).blockingGet()

        then: 'Verify response'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.response.statusCode() == 400
            exception.serviceError == new ServiceValidationErrorModel(
                message: 'Unable to process request because of validation errors',
                validationErrors: [
                    new ServiceValidationError(
                        param: 'key',
                        message: 'must be null'
                    )
                ]
            )
    }

    void "PATCH /groups/{feature_group_key} returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator(textGenerator).generate()

        and:
            featuresIntegrator().registerFeatures(registeredFeatureGroup).blockingGet()

        when: 'has valid patch body'
            FeatureGroup featureGroup = new FeatureGroup(
                name: 'new-name'
            )

        and: 'PATCH /groups/{feature_group_key}'
            featuresIntegrator().patchFeatureGroup('some-key', featureGroup).blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError.param == 'feature_group_key'
            exception.serviceError.message == "No feature group with key 'some-key' exists"
    }

    void "DELETE /groups/{feature_group_key} returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator(textGenerator).generate()

        and:
            registeredFeatureGroup = featuresIntegrator().registerFeatures(registeredFeatureGroup).blockingGet()

        when: 'DELETE /groups/{feature_group_key}'
            String result = featuresIntegrator().deleteFeatureGroup(registeredFeatureGroup.key).blockingGet()

        then: 'Verify response'
            result == ''
    }

    void "DELETE /groups/{feature_group_key} returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'DELETE /groups/{feature_group_key}'
            featuresIntegrator().deleteFeatureGroup('some-key').blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError.param == 'feature_group_key'
            exception.serviceError.message == "No feature group with key 'some-key' exists"
    }

}
