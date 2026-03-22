package dk.sunepoulsen.tes.features.data.generators;

import dk.sunepoulsen.tes.data.generators.DataGenerator;
import dk.sunepoulsen.tes.data.generators.Generators;
import dk.sunepoulsen.tes.data.generators.NumberGenerators;
import dk.sunepoulsen.tes.features.model.Feature;

public class FeatureDataGenerator implements DataGenerator<Feature> {

    static final Integer MIN_TEXT_LENGTH = 5;
    static final Integer MAX_TEXT_LENGTH = 100;

    private final DataGenerator<String> textGenerator;

    public FeatureDataGenerator() {
        this(Generators.textGenerator(NumberGenerators.integerGenerator(MIN_TEXT_LENGTH, MAX_TEXT_LENGTH)));
    }

    public FeatureDataGenerator(DataGenerator<String> textGenerator) {
        this.textGenerator = textGenerator;
    }

    @Override
    public Feature generate() {
        Feature result = new Feature();

        result.setKey(textGenerator.generate());
        result.setName(textGenerator.generate());
        result.setDescription(textGenerator.generate());

        return result;
    }

}
