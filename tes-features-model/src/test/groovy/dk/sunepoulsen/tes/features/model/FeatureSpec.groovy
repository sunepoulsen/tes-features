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
import spock.lang.Unroll

import java.time.ZonedDateTime

class FeatureSpec extends Specification {

    private DefaultValidator validator
    private DataGenerator<ZonedDateTime> datetimeGenerator

    void setup() {
        this.validator = new DefaultValidator()
        this.datetimeGenerator = TimeGenerators.currentZonedDateTimeGenerator()
    }

    void "Validate feature that is valid"() {
        given:
            Feature model = new Feature(
                key: 'key',
                name: 'name'
            )

        when:
            this.validator.validate(model)

        then:
            noExceptionThrown()
    }

    @Unroll
    void "Validate feature that is invalid: #_testcase"() {
        given:
            Feature model = new Feature(
                key: _key,
                name: _name
            )

        when:
            this.validator.validate(model)

        then:
            ConstraintViolationException exception = thrown(ConstraintViolationException)
            ConstraintViolationAssertions.verifyViolations(exception.constraintViolations, _errors)

        where:
            _testcase                        | _key  | _name  | _errors
            'key is null'                    | null  | 'name' | [new ExpectedConstraintViolation('key', 'must not be null')]
            'key contains invalid character' | ';'   | 'name' | [new ExpectedConstraintViolation('key', 'must match "^[^; ]+$"')]
            'name is null'                   | 'key' | null   | [new ExpectedConstraintViolation('name', 'must not be null')]
    }

    void "Validate feature with invalid activations"() {
        given:
            Feature model = new Feature(
                key: 'key',
                name: 'name',
                activations: [
                    new FeatureActivation(
                        id: 10L
                    )
                ]
            )

        when:
            this.validator.validate(model, Default, OnCrudRead)

        then:
            ConstraintViolationException exception = thrown(ConstraintViolationException)
            ConstraintViolationAssertions.verifyViolations(exception.constraintViolations, [
                new ExpectedConstraintViolation('activations[0].enabled', 'must not be null')
            ])
    }

}
