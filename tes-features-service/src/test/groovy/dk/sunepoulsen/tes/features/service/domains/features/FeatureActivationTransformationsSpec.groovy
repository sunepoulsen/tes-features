package dk.sunepoulsen.tes.features.service.domains.features

import dk.sunepoulsen.tes.features.model.FeatureActivation
import dk.sunepoulsen.tes.features.service.domains.persistence.model.FeatureActivationEntity
import spock.lang.Specification

import java.time.ZonedDateTime

class FeatureActivationTransformationsSpec extends Specification {

    private FeatureActivationTransformations sut

    void setup() {
        this.sut = new FeatureActivationTransformations();
    }

    void "Test transaction from activation entity to activation model"() {
        given:
            ZonedDateTime dateTime = ZonedDateTime.now()

        expect:
            sut.toModel(new FeatureActivationEntity(
                id: 17L,
                enabled: true,
                dateTime: dateTime)
            ) == new FeatureActivation(
                id: 17L,
                enabled: true,
                datetime: dateTime
            )
    }

    void "Test transaction from activation model to activation entity"() {
        given:
            ZonedDateTime dateTime = ZonedDateTime.now()
            FeatureActivation activation = new FeatureActivation(
                id: 17L,
                enabled: true,
                datetime: dateTime
            )

        expect:
            sut.toEntity(activation) == new FeatureActivationEntity(
                id: activation.id,
                enabled: activation.enabled,
                dateTime: activation.datetime
            )
    }

    void "Test transaction from activation model to patch activation entity"() {
        given:
            ZonedDateTime dateTime = ZonedDateTime.now()
            FeatureActivation activation = new FeatureActivation(
                id: 17L,
                enabled: true,
                datetime: dateTime
            )

        expect:
            sut.toPatchEntity(activation) == new FeatureActivationEntity(
                id: null,
                enabled: activation.enabled,
                dateTime: activation.datetime
            )
    }

}
