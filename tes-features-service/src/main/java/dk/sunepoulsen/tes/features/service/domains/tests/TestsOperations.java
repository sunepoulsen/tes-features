package dk.sunepoulsen.tes.features.service.domains.tests;

import dk.sunepoulsen.tes.rest.models.ServiceErrorModel;
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudDelete;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Tests", description = "Endpoints to help component & system tests")
@RequestMapping(TestsOperations.TESTS_ENDPOINT_PATH)
@Validated
interface TestsOperations {

    String TESTS_ENDPOINT_PATH = "/tests";
    String PERSISTENCE_PATH = "/persistence";

    @Operation(
        summary = "Delete all records in the persistence storage",
        description = "Deletes all records in the database"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "All records in the persistence storage has been deleted"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden for security reasons",
            content = @Content(
                schema = @Schema(implementation = ServiceErrorModel.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Unable to process this request",
            content = @Content(
                schema = @Schema(implementation = ServiceErrorModel.class)
            )
        )
    })
    @DeleteMapping(PERSISTENCE_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Validated({Default.class, OnCrudDelete.class})
    void deletePersistence();
}
