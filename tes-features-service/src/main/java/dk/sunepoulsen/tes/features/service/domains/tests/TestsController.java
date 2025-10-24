package dk.sunepoulsen.tes.features.service.domains.tests;

import dk.sunepoulsen.tes.features.service.domains.persistence.TestsPersistence;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(TestsOperations.TESTS_ENDPOINT_PATH)
class TestsController implements TestsOperations {

    private final TestsPersistence persistence;

    @Autowired
    public TestsController(TestsPersistence persistence) {
        this.persistence = persistence;
    }

    @DeleteMapping(PERSISTENCE_PATH)
    public void deletePersistence() {
        try {
            persistence.clearDatabase();
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }
}
