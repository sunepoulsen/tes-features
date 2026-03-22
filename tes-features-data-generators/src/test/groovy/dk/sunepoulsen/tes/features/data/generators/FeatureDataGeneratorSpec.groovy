package dk.sunepoulsen.tes.features.data.generators

import dk.sunepoulsen.tes.features.model.Feature
import spock.lang.Specification

class FeatureDataGeneratorSpec extends Specification {

    private FeatureDataGenerator sut

    void setup() {
        this.sut = new FeatureDataGenerator()
    }

    void "Tests generating Feature"() {
        when:
            Feature result = sut.generate()

        then:
            result.key.length() >= FeatureGroupDataGenerator.MIN_TEXT_LENGTH
            result.key.length() < FeatureGroupDataGenerator.MAX_TEXT_LENGTH
            result.name.length() >= FeatureGroupDataGenerator.MIN_TEXT_LENGTH
            result.name.length() < FeatureGroupDataGenerator.MAX_TEXT_LENGTH
            result.description.length() >= FeatureGroupDataGenerator.MIN_TEXT_LENGTH
            result.description.length() < FeatureGroupDataGenerator.MAX_TEXT_LENGTH
    }

}
