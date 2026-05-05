package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.data.generators.NumberGenerators
import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesMockUsers
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.Feature
import dk.sunepoulsen.tes.features.model.RegisterFeature
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.ServiceValidationError
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class PatchFeatureSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider, FeaturesMockUsers {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "PATCH /groups/{feature_group_key}/{feature_key} returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and: 'register feature group and all its features'
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), registeredFeatureGroup).blockingGet()

        and: 'select a feature to patch'
            Integer featureIndex = NumberGenerators.integerGenerator(0, registeredFeatureGroup.features.size()).generate()
            RegisterFeature registeredFeature = registeredFeatureGroup.features[featureIndex]

        when: 'has valid patch body'
            Feature newValues = new Feature(
                name: 'new-name'
            )

        and: 'PATCH /groups/{feature_group_key}/{feature_key}'
            Feature result = featuresServiceIntegrator().features().patchFeature(featuresDefaultUser(), registeredFeatureGroup.key, registeredFeature.key, newValues).blockingGet()

        then: 'Verify response'
            with(result) {
                assert it.key == registeredFeature.key
                assert it.name == newValues.name
                assert it.description == registeredFeature.description
            }
    }

    void "PATCH /groups/{feature_group_key}/{feature_key} returns bad request"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and:
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), registeredFeatureGroup).blockingGet()

        and: 'select a feature to patch'
            Integer featureIndex = NumberGenerators.integerGenerator(0, registeredFeatureGroup.features.size()).generate()
            RegisterFeature registeredFeature = registeredFeatureGroup.features[featureIndex]

        when: 'has invalid patch body'
            Feature newValues = new Feature(
                key: 'not-null'
            )

        and: 'PATCH /groups/{feature_group_key}'
            featuresServiceIntegrator().features().patchFeature(featuresDefaultUser(), registeredFeatureGroup.key, registeredFeature.key, newValues).blockingGet()

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

    void "PATCH /groups/{feature_group_key}/{feature_key} returns not found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and:
            featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), registeredFeatureGroup).blockingGet()

        when: 'has valid patch body'
            Feature newValues = new Feature(
                name: 'new-name'
            )

        and: 'PATCH /groups/{feature_group_key}/{feature_key}'
            featuresServiceIntegrator().features().patchFeature(featuresDefaultUser(), 'some-key', 'some-other-key', newValues).blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError.param == null
            exception.serviceError.message == "No feature with feature group 'some-key' and feature 'some-other-key' exists"
    }

}
