package dk.sunepoulsen.tes.features.ct


import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.EnvelopeFeatureGroup
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class GetFeatureGroupsSpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "GET /groups returns OK"() {
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

        when: 'GET /groups'
            EnvelopeFeatureGroup result = featuresServiceIntegrator().featureGroups().getFeatureGroups().blockingGet()

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

}
