package dk.sunepoulsen.tes.features.ct


import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesMockUsers
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.FeatureGroup
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.ServiceValidationError
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class PatchFeatureGroupSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider, FeaturesMockUsers {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "PATCH /groups/{feature_group_key} returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and:
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), registeredFeatureGroup).blockingGet()

        when: 'has valid patch body'
            FeatureGroup featureGroup = new FeatureGroup(
                name: 'new-name'
            )

        and: 'PATCH /groups/{feature_group_key}'
            FeatureGroup result = featuresServiceIntegrator().featureGroups().patchFeatureGroup(featuresDefaultUser(), registeredFeatureGroup.key, featureGroup).blockingGet()

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
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and:
            registeredFeatureGroup = featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), registeredFeatureGroup).blockingGet()

        when: 'has invalid patch body'
            FeatureGroup featureGroup = new FeatureGroup(
                key: 'not-null'
            )

        and: 'PATCH /groups/{feature_group_key}'
            featuresServiceIntegrator().featureGroups().patchFeatureGroup(featuresDefaultUser(), registeredFeatureGroup.key, featureGroup).blockingGet()

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
            RegisterFeatureGroup registeredFeatureGroup = new RegisterFeatureGroupDataGenerator().generate()

        and:
            featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), registeredFeatureGroup).blockingGet()

        when: 'has valid patch body'
            FeatureGroup featureGroup = new FeatureGroup(
                name: 'new-name'
            )

        and: 'PATCH /groups/{feature_group_key}'
            featuresServiceIntegrator().featureGroups().patchFeatureGroup(featuresDefaultUser(), 'some-key', featureGroup).blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError.param == 'feature_group_key'
            exception.serviceError.message == "No feature group with key 'some-key' exists"
    }

}
