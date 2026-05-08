package dk.sunepoulsen.tes.features.ct

import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator
import dk.sunepoulsen.tes.features.deployment.FeaturesMockUsers
import dk.sunepoulsen.tes.features.deployment.FeaturesServiceIntegratorProvider
import dk.sunepoulsen.tes.features.deployment.FeaturesTestsIntegratorProvider
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientForbiddenException
import dk.sunepoulsen.tes.rest.integrations.exceptions.ClientUnauthorizedException
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel
import spock.lang.Specification

class SecuritySpec extends Specification implements FeaturesServiceIntegratorProvider, FeaturesTestsIntegratorProvider, FeaturesMockUsers {

    void setup() {
        featuresTestsIntegrator().deletePersistence().blockingAwait()
    }

    void "PUT /features returns 403 because of missing roles"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup featureGroup = new RegisterFeatureGroupDataGenerator().generate()

        when: 'Call PUT /features'
            featuresServiceIntegrator().features().registerFeatures(featuresUnknownUser(), featureGroup).blockingGet()

        then: 'Verify response'
            ClientForbiddenException exception = thrown(ClientForbiddenException)
            exception.response.statusCode() == 403
            exception.serviceError == new ServiceErrorModel(
                message: 'Service returned response with status 403'
            )
    }

    void "PUT /features returns 401 because of bad signature"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        and: 'valid feature group'
            RegisterFeatureGroup featureGroup = new RegisterFeatureGroupDataGenerator().generate()

        when: 'Call PUT /features'
            featuresServiceIntegrator().features().registerFeatures(featuresHackerUser(), featureGroup).blockingGet()

        then: 'Verify response'
            ClientUnauthorizedException exception = thrown(ClientUnauthorizedException)
            exception.response.statusCode() == 401
            exception.serviceError == new ServiceErrorModel(
                message: 'Service returned response with status 401'
            )
    }

    void "GET /groups/{feature_group_key} returns 403 because of missing roles"() {
        given: 'Services is available'
            isFeaturesServiceAvailable()

        when: 'Call GET /groups/{feature_group_key}'
            featuresServiceIntegrator().featureGroups().getFeatureGroup(featuresUnknownUser(), 'wrong-key').blockingGet()

        then: 'Verify response'
            ClientForbiddenException exception = thrown(ClientForbiddenException)
            exception.response.statusCode() == 403
            exception.serviceError == new ServiceErrorModel(
                message: 'Service returned response with status 403'
            )
    }

}
