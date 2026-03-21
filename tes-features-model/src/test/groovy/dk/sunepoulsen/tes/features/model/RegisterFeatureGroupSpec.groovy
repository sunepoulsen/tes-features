package dk.sunepoulsen.tes.features.model

import dk.sunepoulsen.tes.data.generators.DataGenerator
import dk.sunepoulsen.tes.data.generators.TimeGenerators
import dk.sunepoulsen.tes.rest.models.validation.DefaultValidator
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudRead
import dk.sunepoulsen.tes.validation.tests.ConstraintViolationAssertions
import dk.sunepoulsen.tes.validation.tests.ExpectedConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.groups.Default
import spock.lang.Specification

import java.time.ZonedDateTime

class RegisterFeatureGroupSpec extends Specification {

    private DefaultValidator validator
    private DataGenerator<ZonedDateTime> datetimeGenerator

    void setup() {
        this.validator = new DefaultValidator()
        this.datetimeGenerator = TimeGenerators.currentZonedDateTimeGenerator()
    }

    void "Validate feature group that is valid"() {
        given:
            RegisterFeatureGroup model = new RegisterFeatureGroup(
                key: 'key',
                name: 'name'
            )

        when:
            this.validator.validate(model)

        then:
            noExceptionThrown()
    }

    void "Validate feature group with invalid features"() {
        given:
            RegisterFeatureGroup model = new RegisterFeatureGroup(
                key: 'key',
                name: 'name',
                features: [
                    new RegisterFeature(
                        key: 'key'
                    )
                ]
            )

        when:
            this.validator.validate(model, Default, OnCrudRead)

        then:
            ConstraintViolationException exception = thrown(ConstraintViolationException)
            ConstraintViolationAssertions.verifyViolations(exception.constraintViolations, [
                new ExpectedConstraintViolation('features[0].name', 'must not be null')
            ])
    }

}
