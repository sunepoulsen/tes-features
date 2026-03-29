package dk.sunepoulsen.tes.features.model

import dk.sunepoulsen.tes.data.generators.DataGenerator
import dk.sunepoulsen.tes.data.generators.TimeGenerators
import dk.sunepoulsen.tes.rest.models.validation.DefaultValidator
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudCreate
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudRead
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudUpdate
import dk.sunepoulsen.tes.validation.tests.ConstraintViolationAssertions
import dk.sunepoulsen.tes.validation.tests.ExpectedConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.groups.Default
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
    void "Validate feature activation that is valid: #_testcase"() {
        given:
            FeatureActivation model = new FeatureActivation(
                id: _id,
                enabled: _enabled,
                datetime: _datetime
            )

        when:
            this.validator.validate(model, Default, _group)

        then:
            noExceptionThrown()

        where:
            _testcase                   | _group             | _id  | _enabled | _datetime
            'OnCrudCreate'              | OnCrudCreate.class | null | true     | ZonedDateTime.now()
            'OnCrudRead'                | OnCrudRead.class   | 27L  | true     | ZonedDateTime.now()
            'OnCrudUpdate: Null values' | OnCrudUpdate.class | null | null     | null
    }

    @Unroll
    void "Validate invalid feature activation: #_testcase"() {
        given:
            FeatureActivation model = new FeatureActivation(
                id: _id,
                enabled: _enabled,
                datetime: _datetime
            )

        when:
            this.validator.validate(model, Default, _group)

        then:
            ConstraintViolationException exception = thrown(ConstraintViolationException)
            ConstraintViolationAssertions.verifyViolations(exception.constraintViolations, [
                new ExpectedConstraintViolation(_propertyError, _errorMessage)
            ])

        where:
            _testcase                        | _group             | _id  | _enabled | _datetime           | _propertyError | _errorMessage
            'OnCrudCreate: Id is not null'   | OnCrudCreate.class | 27L  | true     | ZonedDateTime.now() | 'id'           | 'must be null'
            'OnCrudCreate: Enabled is null'  | OnCrudCreate.class | null | null     | ZonedDateTime.now() | 'enabled'      | 'must not be null'
            'OnCrudCreate: Datetime is null' | OnCrudCreate.class | null | true     | null                | 'datetime'     | 'must not be null'
            'OnCrudRead: Id is null'         | OnCrudRead.class   | null | true     | ZonedDateTime.now() | 'id'           | 'must not be null'
            'OnCrudRead: Enabled is null'    | OnCrudRead.class   | 27L  | null     | ZonedDateTime.now() | 'enabled'      | 'must not be null'
            'OnCrudRead: Datetime is null'   | OnCrudRead.class   | 27L  | true     | null                | 'datetime'     | 'must not be null'
            'OnCrudUpdate: Id is not null'   | OnCrudUpdate.class | 27L  | true     | ZonedDateTime.now() | 'id'           | 'must be null'
    }

}
