package dk.sunepoulsen.tes.features.model

import dk.sunepoulsen.tes.rest.models.validation.DefaultValidator
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudCreate
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudRead
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudUpdate
import dk.sunepoulsen.tes.data.generators.DataGenerator
import dk.sunepoulsen.tes.data.generators.TimeGenerators
import dk.sunepoulsen.tes.validation.tests.ConstraintViolationAssertions
import dk.sunepoulsen.tes.validation.tests.ExpectedConstraintViolation
import jakarta.validation.ConstraintViolationException
import spock.lang.Specification
import spock.lang.Unroll

import java.time.ZonedDateTime

class FeatureActivationSpec extends Specification {

    private DefaultValidator validator
    private DataGenerator<ZonedDateTime> datetimeGenerator

    void setup() {
        this.validator = new DefaultValidator()
        this.datetimeGenerator = TimeGenerators.currentZonedDateTimeGenerator()
    }

    @Unroll
    void "Validate with group #_testcase is valid"() {
        given:
            FeatureActivation model = FeatureActivation.builder()
                .id(_id)
                .enabled(_enabled)
                .datetime(this.datetimeGenerator.generate())
                .build()

        when:
            this.validator.validate(model, _group)

        then:
            noExceptionThrown()

        where:
            _testcase      | _group       | _id  | _enabled
            'OnCrudCreate' | OnCrudCreate | null | true
            'OnCrudRead'   | OnCrudRead   | 1L   | false
    }

    @Unroll
    void "Validate with group OnCrudCreate is invalid: #_testcase"() {
        given:
            FeatureActivation model = FeatureActivation.builder()
                .id(_id)
                .enabled(_enabled)
                .datetime(this.datetimeGenerator.generate())
                .build()

        when:
            this.validator.validate(model, OnCrudCreate)

        then:
            ConstraintViolationException exception = thrown(ConstraintViolationException)
            ConstraintViolationAssertions.verifyViolations(exception.constraintViolations, _errors)

        where:
            _testcase         | _id  | _enabled | _errors
            'id is not null'  | 10L  | true     | [new ExpectedConstraintViolation('id', 'must be null')]
            'enabled is null' | null | null     | [new ExpectedConstraintViolation('enabled', 'must not be null')]
    }

    @Unroll
    void "Validate with group OnCrudRead is invalid: #_testcase"() {
        given:
            FeatureActivation model = FeatureActivation.builder()
                .id(_id)
                .enabled(_enabled)
                .datetime(this.datetimeGenerator.generate())
                .build()

        when:
            this.validator.validate(model, OnCrudRead)

        then:
            ConstraintViolationException exception = thrown(ConstraintViolationException)
            ConstraintViolationAssertions.verifyViolations(exception.constraintViolations, _errors)

        where:
            _testcase         | _id  | _enabled | _errors
            'id is null'      | null | true     | [new ExpectedConstraintViolation('id', 'must not be null')]
            'enabled is null' | 10L  | null     | [new ExpectedConstraintViolation('enabled', 'must not be null')]
    }

    @Unroll
    void "Validate with group OnCrudUpdate is valid: #_testcase"() {
        given:
            FeatureActivation model = FeatureActivation.builder()
                .id(null)
                .enabled(_enabled)
                .datetime(this.datetimeGenerator.generate())
                .build()

        when:
            this.validator.validate(model, OnCrudUpdate)

        then:
            noExceptionThrown()

        where:
            _testcase             | _enabled
            'All values are null' | null
            'enabled is null'     | null
    }

    void "Validate with group OnCrudUpdate is invalid"() {
        given:
            FeatureActivation model = FeatureActivation.builder()
                .id(10L)
                .enabled(null)
                .datetime(null)
                .build()

        when:
            this.validator.validate(model, OnCrudUpdate)

        then:
            ConstraintViolationException exception = thrown(ConstraintViolationException)
            ConstraintViolationAssertions.verifyViolations(exception.constraintViolations, [
                new ExpectedConstraintViolation('id', 'must be null')
            ])
    }

}
