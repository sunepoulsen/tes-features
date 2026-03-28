package dk.sunepoulsen.tes.features.ct


import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.models.ServiceValidationError
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class RegisterFeaturesSpec extends Specification implements FeaturesIntegratorProvider, FeaturesTestsIntegratorProvider {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingGet()
    }

    void "PUT /features returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup featureGroup = new RegisterFeatureGroupDataGenerator().generate()

        when: 'Call PUT /features'
            RegisterFeatureGroup result = featuresIntegrator().registerFeatures(featureGroup).blockingGet()

        then: 'Verify response'
            with(result) {
                assert it.key == featureGroup.key
                assert it.name == featureGroup.name
                assert it.description == featureGroup.description
                it.features.eachWithIndex { feature, index ->
                    assert feature.key == featureGroup.features[index].key
                    assert feature.name == featureGroup.features[index].name
                    assert feature.description == featureGroup.features[index].description
                    feature.activations.eachWithIndex { FeatureActivation activation, idx ->
                        assert activation.id > 0
                        assert activation.enabled == featureGroup.features[index].activations[idx].enabled
                        assert activation.datetime.isEqual(featureGroup.features[index].activations[idx].datetime)
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
            featuresIntegrator().registerFeatures(featureGroup).blockingGet()

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
