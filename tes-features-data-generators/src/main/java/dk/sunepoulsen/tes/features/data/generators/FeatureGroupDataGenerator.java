package dk.sunepoulsen.tes.features.data.generators;

import dk.sunepoulsen.tes.data.generators.DataGenerator;
import dk.sunepoulsen.tes.data.generators.Generators;
import dk.sunepoulsen.tes.data.generators.NumberGenerators;
import dk.sunepoulsen.tes.features.model.FeatureGroup;

public class FeatureGroupDataGenerator implements DataGenerator<FeatureGroup> {

    static final Integer MIN_TEXT_LENGTH = 5;
    static final Integer MAX_TEXT_LENGTH = 100;

    private final DataGenerator<String> textGenerator;

    public FeatureGroupDataGenerator() {
        this(Generators.textGenerator(NumberGenerators.integerGenerator(MIN_TEXT_LENGTH, MAX_TEXT_LENGTH)));
    }

    public FeatureGroupDataGenerator(DataGenerator<String> textGenerator) {
        this.textGenerator = textGenerator;
    }

    @Override
    public FeatureGroup generate() {
        FeatureGroup result = new FeatureGroup();

        result.setKey(textGenerator.generate());
        result.setName(textGenerator.generate());
        result.setDescription(textGenerator.generate());

        return result;
    }

}
