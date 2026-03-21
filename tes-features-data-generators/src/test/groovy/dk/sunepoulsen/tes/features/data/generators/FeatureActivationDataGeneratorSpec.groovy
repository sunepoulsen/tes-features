package dk.sunepoulsen.tes.features.data.generators

import dk.sunepoulsen.tes.features.model.FeatureActivation
import spock.lang.Specification

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class FeatureActivationDataGeneratorSpec extends Specification {

    private FeatureActivationDataGenerator sut

    void setup() {
        this.sut = new FeatureActivationDataGenerator()
    }

    void "Tests generating FeatureActivation with current datetime"() {
        when:
            FeatureActivation result = sut.generate()

        then:
            result.enabled
            result.datetime.until(ZonedDateTime.now(), ChronoUnit.MILLIS) < 100
            result.datetime == result.datetime.truncatedTo(ChronoUnit.MICROS)
    }

}
