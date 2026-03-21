package dk.sunepoulsen.tes.features.data.generators;

import dk.sunepoulsen.tes.data.generators.DataGenerator;
import dk.sunepoulsen.tes.data.generators.DataListGenerator;
import dk.sunepoulsen.tes.data.generators.Generators;
import dk.sunepoulsen.tes.data.generators.NumberGenerators;
import dk.sunepoulsen.tes.features.model.RegisterFeature;

public class RegisterFeatureDataGenerator implements DataGenerator<RegisterFeature> {

    private final DataGenerator<String> textGenerator;

    public RegisterFeatureDataGenerator() {
        this(Generators.textGenerator(NumberGenerators.integerGenerator(5, 100)));
    }

    public RegisterFeatureDataGenerator(DataGenerator<String> textGenerator) {
        this.textGenerator = textGenerator;
    }

    @Override
    public RegisterFeature generate() {
        RegisterFeature result = new RegisterFeature();

        result.setKey(textGenerator.generate());
        result.setName(textGenerator.generate());
        result.setDescription(textGenerator.generate());
        result.setActivations(new DataListGenerator<>(
            NumberGenerators.integerGenerator(1, 5),
            integer -> new FeatureActivationDataGenerator().generate()
        ).generate());

        return result;
    }

}
