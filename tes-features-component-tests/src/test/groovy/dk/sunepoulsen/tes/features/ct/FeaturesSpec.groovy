package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.data.generators.CharacterGenerator
import dk.sunepoulsen.tes.data.generators.DataGenerator
import dk.sunepoulsen.tes.data.generators.Generators
import dk.sunepoulsen.tes.data.generators.NumberGenerators
import dk.sunepoulsen.tes.features.data.generators.FeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.features.model.FeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientBadRequestException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientNotFoundException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import dk.sunepoulsen.tes.rest.models.ServiceValidationError
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class FeaturesSpec extends Specification implements FeaturesIntegratorProvider, FeaturesTestsIntegratorProvider {

    private DataGenerator<String> textGenerator

    void setup() {
        this.textGenerator = Generators.textGenerator(
            [CharacterGenerator.URI_PATH_CHARECTERS],
            NumberGenerators.integerGenerator(5, 50)
        )
        featuresTestsIntegrator().deletePersistence().blockingGet()
    }

    void "PUT /features returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            FeatureGroup featureGroup = new FeatureGroupDataGenerator(textGenerator).generate()

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

    void "GET /feature-groups/{feature_group_key} returns OK"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            FeatureGroup featureGroup = new FeatureGroupDataGenerator(textGenerator).generate()

        and:
            featureGroup = featuresIntegrator().registerFeatures(featureGroup).blockingGet()

        when: 'GET /feature-groups/{feature_group_key}'
            FeatureGroup result = featuresIntegrator().getFeatureGroup(featureGroup.key).blockingGet()

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
                        assert activation.id == featureGroup.features[index].activations[idx].id
                        assert activation.enabled == featureGroup.features[index].activations[idx].enabled
                        assert activation.datetime.isEqual(featureGroup.features[index].activations[idx].datetime)
                    }
                }
                it.activations.eachWithIndex { FeatureActivation activation, idx ->
                    assert activation.id == featureGroup.activations[idx].id
                    assert activation.enabled == featureGroup.activations[idx].enabled
                    assert activation.datetime.isEqual(featureGroup.activations[idx].datetime)
                }
            }
    }

    void "GET /feature-groups/{feature_group_key} returns Bad Request"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'Call GET /feature-groups/{feature_group_key}'
            featuresIntegrator().getFeatureGroup('wrong;key').blockingGet()

        then: 'Verify response'
            ClientBadRequestException exception = thrown(ClientBadRequestException)
            exception.response.statusCode() == 400
            exception.serviceError == new ServiceValidationErrorModel()
    }

    void "GET /feature-groups/{feature_group_key} returns Not Found"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'Call GET /feature-groups/{feature_group_key}'
            featuresIntegrator().getFeatureGroup('wrong-key').blockingGet()

        then: 'Verify response'
            ClientNotFoundException exception = thrown(ClientNotFoundException)
            exception.response.statusCode() == 404
            exception.serviceError == new ServiceErrorModel(
                param: 'key',
                message: 'No feature group exists with key: wrong-key'
            )
    }

}
