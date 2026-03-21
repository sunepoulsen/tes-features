package dk.sunepoulsen.tes.features.data.generators


import dk.sunepoulsen.tes.features.model.RegisterFeature
import spock.lang.Specification

class RegisterFeatureDataGeneratorSpec extends Specification {

    private RegisterFeatureDataGenerator sut

    void setup() {
        this.sut = new RegisterFeatureDataGenerator()
    }

    void "Tests generating RegisterFeature"() {
        when:
            RegisterFeature result = sut.generate()

        then:
            result.key.length() >= RegisterFeatureDataGenerator.MIN_TEXT_LENGTH
            result.key.length() < RegisterFeatureDataGenerator.MAX_TEXT_LENGTH
            result.name.length() >= RegisterFeatureDataGenerator.MIN_TEXT_LENGTH
            result.name.length() < RegisterFeatureDataGenerator.MAX_TEXT_LENGTH
            result.description.length() >= RegisterFeatureDataGenerator.MIN_TEXT_LENGTH
            result.description.length() < RegisterFeatureDataGenerator.MAX_TEXT_LENGTH

            result.activations.size() >= 1
            result.activations.size() < 5
    }

}
