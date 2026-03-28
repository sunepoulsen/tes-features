package dk.sunepoulsen.tes.features.data.generators;

import dk.sunepoulsen.tes.data.generators.*;
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup;

import java.util.List;

public class RegisterFeatureGroupDataGenerator implements DataGenerator<RegisterFeatureGroup> {

    private static final DataGenerator<String> DEFAULT_TEXT_GENERATOR = Generators.textGenerator(
        List.of(CharacterGenerator.URI_PATH_CHARECTERS),
        NumberGenerators.integerGenerator(5, 50)
    );

    private final DataGenerator<String> textGenerator;

    public RegisterFeatureGroupDataGenerator() {
        this(DEFAULT_TEXT_GENERATOR);
    }

    public RegisterFeatureGroupDataGenerator(DataGenerator<String> textGenerator) {
        this.textGenerator = textGenerator;
    }

    @Override
    public RegisterFeatureGroup generate() {
        RegisterFeatureGroup result = new RegisterFeatureGroup();

        result.setKey(textGenerator.generate());
        result.setName(textGenerator.generate());
        result.setDescription(textGenerator.generate());
        result.setFeatures(new DataListGenerator<>(
            NumberGenerators.integerGenerator(1, 5),
            integer -> new RegisterFeatureDataGenerator(textGenerator).generate()
        ).generate());
        result.setActivations(new DataListGenerator<>(
            NumberGenerators.integerGenerator(1, 5),
            integer -> new FeatureActivationDataGenerator().generate()
        ).generate());

        return result;
    }

}
