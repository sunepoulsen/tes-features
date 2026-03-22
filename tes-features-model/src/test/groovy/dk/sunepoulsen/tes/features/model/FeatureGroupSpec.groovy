package dk.sunepoulsen.tes.features.model

import dk.sunepoulsen.tes.data.generators.DataGenerator
import dk.sunepoulsen.tes.data.generators.TimeGenerators
import dk.sunepoulsen.tes.rest.models.validation.DefaultValidator
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudRead
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudUpdate
import dk.sunepoulsen.tes.validation.tests.ConstraintViolationAssertions
import dk.sunepoulsen.tes.validation.tests.ExpectedConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.groups.Default
import spock.lang.Specification
import spock.lang.Unroll

import java.time.ZonedDateTime

class FeatureGroupSpec extends Specification {

    private DefaultValidator validator
    private DataGenerator<ZonedDateTime> datetimeGenerator

    void setup() {
        this.validator = new DefaultValidator()
        this.datetimeGenerator = TimeGenerators.currentZonedDateTimeGenerator()
    }

    @Unroll
    void "Validate feature group that is valid: #_testcase"() {
        given:
            FeatureGroup model = new FeatureGroup(
                key: _key,
                name: _name
            )

        when:
            this.validator.validate(model, Default, _group)

        then:
            noExceptionThrown()

        where:
            _testcase                    | _group             | _key  | _name
            'OnCrudRead'                 | OnCrudRead.class   | 'key' | 'name'
            'OnCrudUpdate: Name is null' | OnCrudUpdate.class | null  | null
    }

    @Unroll
    void "Validate invalid feature group: #_testcase"() {
        given:
            FeatureGroup model = new FeatureGroup(
                key: _key,
                name: _name,
                description: 'description'
            )

        when:
            this.validator.validate(model, Default, _group)

        then:
            ConstraintViolationException exception = thrown(ConstraintViolationException)
            ConstraintViolationAssertions.verifyViolations(exception.constraintViolations, [
                new ExpectedConstraintViolation(_propertyError, _errorMessage)
            ])

        where:
            _testcase                       | _group             | _key  | _name  | _propertyError | _errorMessage
            'OnCrudRead: Key is null'       | OnCrudRead.class   | null  | 'name' | 'key'          | 'must not be null'
            'OnCrudRead: Name is null'      | OnCrudRead.class   | 'key' | null   | 'name'         | 'must not be null'
            'OnCrudUpdate: Key is not null' | OnCrudUpdate.class | 'key' | 'name' | 'key'          | 'must be null'
    }

}
