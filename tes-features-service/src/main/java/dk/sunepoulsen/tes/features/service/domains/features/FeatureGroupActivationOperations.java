package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureActivation;
import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.features.service.domains.features.openapi.FeatureGroups;
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel;
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel;
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudCreate;
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Operations for feature group activations.
 */
@FeatureGroups
@RequestMapping(FeatureGroupActivationOperations.ENDPOINT_PATH)
@Validated
public interface FeatureGroupActivationOperations {
    String ENDPOINT_PATH = FeatureGroupsOperations.FEATURE_GROUPS_ENDPOINT_PATH + "/{feature_group_key}/activations";

    /**
     * Creates a new activation for the given feature group.
     *
     * @param key           the feature group key
     * @param newActivation the activation to create
     * @return the created activation
     */
    @Operation(
        summary = "Creates a new activation for the given feature group",
        description = """
                Creates a new activation for a feature group
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Successfully created the new activation"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request because of validation errors",
            content = @Content(
                schema = @Schema(implementation = ServiceValidationErrorModel.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "If the feature group does not exist",
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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated({Default.class, OnCrudCreate.class})
    DeferredResult<FeatureActivation> createActivation(
        @Valid @PathVariable("feature_group_key") final String key,
        @Valid @RequestBody FeatureActivation newActivation
    );

    /**
     * Returns a list of all activations for the given feature group.
     *
     * @param key the feature group key
     * @return the activations
     */
    @Operation(
        summary = "Returns a list of all activations for the given feature group",
        description = """
                Returns all found activations.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully returned all found activations"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request because of invalid feature group key",
            content = @Content(
                schema = @Schema(implementation = ServiceValidationErrorModel.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No feature group exist with the given key",
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
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Validated({Default.class, OnCrudRead.class})
    DeferredResult<EnvelopeFeatureActivation> getActivations(@Valid @PathVariable("feature_group_key") final String key);

}
