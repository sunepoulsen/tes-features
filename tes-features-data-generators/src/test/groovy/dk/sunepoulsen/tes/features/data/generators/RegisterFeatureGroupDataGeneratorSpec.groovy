package dk.sunepoulsen.tes.features.data.generators


import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup
import spock.lang.Specification

class RegisterFeatureGroupDataGeneratorSpec extends Specification {

    private RegisterFeatureGroupDataGenerator sut

    void setup() {
        this.sut = new RegisterFeatureGroupDataGenerator()
    }

    void "Tests generating RegisterFeatureGroup"() {
        when:
            RegisterFeatureGroup result = sut.generate()

        then:
            result.key.length() >= RegisterFeatureDataGenerator.MIN_TEXT_LENGTH
            result.key.length() < RegisterFeatureDataGenerator.MAX_TEXT_LENGTH
            result.name.length() >= RegisterFeatureDataGenerator.MIN_TEXT_LENGTH
            result.name.length() < RegisterFeatureDataGenerator.MAX_TEXT_LENGTH
            result.description.length() >= RegisterFeatureDataGenerator.MIN_TEXT_LENGTH
            result.description.length() < RegisterFeatureDataGenerator.MAX_TEXT_LENGTH

            result.features.size() >= 1
            result.features.size() < 5
            result.activations.size() >= 1
            result.activations.size() < 5
    }

}
