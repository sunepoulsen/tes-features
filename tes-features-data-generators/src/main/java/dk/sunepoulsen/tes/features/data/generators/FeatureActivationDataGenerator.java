package dk.sunepoulsen.tes.features.data.generators;

import dk.sunepoulsen.tes.data.generators.DataGenerator;
import dk.sunepoulsen.tes.data.generators.TimeGenerators;
import dk.sunepoulsen.tes.features.model.FeatureActivation;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class FeatureActivationDataGenerator implements DataGenerator<FeatureActivation> {

    private final DataGenerator<ZonedDateTime> dateTimeDataGenerator;

    public FeatureActivationDataGenerator() {
        this(TimeGenerators.currentZonedDateTimeGenerator());
    }

    public FeatureActivationDataGenerator(DataGenerator<ZonedDateTime> dateTimeDataGenerator) {
        this.dateTimeDataGenerator = dateTimeDataGenerator;
    }

    @Override
    public FeatureActivation generate() {
        FeatureActivation result = new FeatureActivation();

        result.setEnabled(true);
        result.setDatetime(dateTimeDataGenerator.generate().truncatedTo(ChronoUnit.MICROS));

        return result;
    }

}
