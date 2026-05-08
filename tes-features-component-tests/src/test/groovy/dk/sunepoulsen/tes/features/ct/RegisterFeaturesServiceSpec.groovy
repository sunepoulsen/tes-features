package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesMockUsers
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.features.model.RegisterFeature
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.models.ServiceValidationError
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import spock.lang.Specification

class RegisterFeaturesServiceSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider, FeaturesMockUsers {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "PUT /features returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup featureGroup = new RegisterFeatureGroupDataGenerator().generate()

        when: 'Call PUT /features'
            RegisterFeatureGroup result = featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), featureGroup).blockingGet()

        then: 'Verify response'
            with(result) {
                assert it.key == featureGroup.key
                assert it.name == featureGroup.name
                assert it.description == featureGroup.description
                it.features.each { feature ->
                    RegisterFeature expectedFeature = featureGroup.features.find { it.key == feature.key }
                    assert feature.key == expectedFeature.key
                    assert feature.name == expectedFeature.name
                    assert feature.description == expectedFeature.description
                    feature.activations.eachWithIndex { FeatureActivation activation, idx ->
                        assert activation.id > 0
                        assert activation.enabled == expectedFeature.activations[idx].enabled
                        assert activation.datetime.isEqual(expectedFeature.activations[idx].datetime)
                    }
                }
                it.activations.eachWithIndex { FeatureActivation activation, idx ->
                    assert activation.id > 0
                    assert activation.enabled == featureGroup.activations[idx].enabled
                    assert activation.datetime.isEqual(featureGroup.activations[idx].datetime)
                }
            }
    }

    void "PUT /features returns Bad request"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'invalid feature group'
            RegisterFeatureGroup featureGroup = new RegisterFeatureGroup()

        when: 'Call PUT /features'
            featuresServiceIntegrator().features().registerFeatures(featuresDefaultUser(), featureGroup).blockingGet()

        then: 'Verify response'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.response.statusCode() == 400
            exception.serviceError == new ServiceValidationErrorModel(
                message: 'Unable to process request because of validation errors',
                validationErrors: [
                    new ServiceValidationError(
                        param: 'key',
                        message: 'must not be null'
                    ),
                    new ServiceValidationError(
                        param: 'name',
                        message: 'must not be null'
                    )
                ]
            )
    }

}
