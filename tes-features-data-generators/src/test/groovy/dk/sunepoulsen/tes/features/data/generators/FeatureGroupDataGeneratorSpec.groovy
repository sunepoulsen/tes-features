package dk.sunepoulsen.tes.features.data.generators


import dk.sunepoulsen.tes.features.model.FeatureGroup
import spock.lang.Specification

class FeatureGroupDataGeneratorSpec extends Specification {

    private FeatureGroupDataGenerator sut

    void setup() {
        this.sut = new FeatureGroupDataGenerator()
    }

    void "Tests generating FeatureGroup"() {
        when:
            FeatureGroup result = sut.generate()

        then:
            result.key.length() >= FeatureGroupDataGenerator.MIN_TEXT_LENGTH
            result.key.length() < FeatureGroupDataGenerator.MAX_TEXT_LENGTH
            result.name.length() >= FeatureGroupDataGenerator.MIN_TEXT_LENGTH
            result.name.length() < FeatureGroupDataGenerator.MAX_TEXT_LENGTH
            result.description.length() >= FeatureGroupDataGenerator.MIN_TEXT_LENGTH
            result.description.length() < FeatureGroupDataGenerator.MAX_TEXT_LENGTH
    }

}
