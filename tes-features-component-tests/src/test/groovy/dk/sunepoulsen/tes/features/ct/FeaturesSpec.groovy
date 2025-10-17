package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.data.generators.DataGenerator
import dk.sunepoulsen.tes.data.generators.Generators
import dk.sunepoulsen.tes.data.generators.NumberGenerators
import dk.sunepoulsen.tes.features.data.generators.FeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.features.model.FeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.models.ServiceValidationError
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import spock.lang.Specification

class FeaturesSpec extends Specification implements FeaturesIntegratorProvider, FeaturesTestsIntegratorProvider {

    private DataGenerator<String> textGenerator

    void setup() {
        this.textGenerator = Generators.textGenerator(NumberGenerators.integerGenerator(10, 50))
        featuresTestsIntegrator().deletePersistence().blockingGet()
    }

    void "PUT /features returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            FeatureGroup featureGroup = new FeatureGroupDataGenerator().generate()

        when: 'Call PUT /features'
            FeatureGroup result = featuresIntegrator().registerFeatures(featureGroup).blockingGet()

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
            FeatureGroup featureGroup = new FeatureGroup()

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
