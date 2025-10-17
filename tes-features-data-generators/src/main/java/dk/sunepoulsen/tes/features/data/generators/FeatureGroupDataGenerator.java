package dk.sunepoulsen.tes.features.data.generators;

import dk.sunepoulsen.tes.data.generators.DataGenerator;
import dk.sunepoulsen.tes.data.generators.DataListGenerator;
import dk.sunepoulsen.tes.data.generators.Generators;
import dk.sunepoulsen.tes.data.generators.NumberGenerators;
import dk.sunepoulsen.tes.features.model.FeatureGroup;

public class FeatureGroupDataGenerator implements DataGenerator<FeatureGroup> {

    private final DataGenerator<String> textGenerator;

    public FeatureGroupDataGenerator() {
        this(Generators.textGenerator(NumberGenerators.integerGenerator(5, 100)));
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
        result.setFeatures(new DataListGenerator<>(
            NumberGenerators.integerGenerator(1, 5),
            integer -> new FeatureDataGenerator().generate()
        ).generate());
        result.setActivations(new DataListGenerator<>(
            NumberGenerators.integerGenerator(1, 5),
            integer -> new FeatureActivationDataGenerator().generate()
        ).generate());

        return result;
    }

}
