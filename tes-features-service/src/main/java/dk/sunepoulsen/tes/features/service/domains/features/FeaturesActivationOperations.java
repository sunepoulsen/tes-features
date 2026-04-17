package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureActivation;
import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.features.service.domains.features.openapi.FeatureActivations;
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel;
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel;
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudCreate;
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudRead;
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudUpdate;
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
 * Operations for feature activations.
 */
@FeatureActivations
@RequestMapping(FeaturesActivationOperations.ENDPOINT_PATH)
@Validated
public interface FeaturesActivationOperations {
    String ENDPOINT_PATH = FeaturesOperations.FEATURE_ENDPOINT_PATH + "/activations";

    /**
     * Creates a new activation for the given feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @param newActivation   the activation to create
     * @return the created activation
     */
    @Operation(
        summary = "Creates a new activation for the given feature",
        description = """
                Creates a new activation for a feature
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Successfully created the new activation",
            content = @Content(
                schema = @Schema(implementation = FeatureActivation.class)
            )
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
    DeferredResult<FeatureActivation> createActivation(@Valid @PathVariable("feature_group_key") final String featureGroupKey,
                                                       @Valid @PathVariable("feature_key") final String featureKey,
                                                       @Valid @RequestBody FeatureActivation newActivation);

    /**
     * Returns a list of all activations for the given feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @return the activations
     */
    @Operation(
        summary = "Returns a list of all activations for the given feature",
        description = """
                Returns all found activations.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully returned all found activations",
            content = @Content(
                schema = @Schema(implementation = EnvelopeFeatureActivation.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request because of invalid keys",
            content = @Content(
                schema = @Schema(implementation = ServiceValidationErrorModel.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No feature exist with the given keys",
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
    DeferredResult<EnvelopeFeatureActivation> getActivations(@Valid @PathVariable("feature_group_key") final String featureGroupKey,
                                                             @Valid @PathVariable("feature_key") final String featureKey);

    /**
     * Returns a specific activation for the given feature.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @param activationId    the activation id
     * @return the activation
     */
    @Operation(
        summary = "Returns a specific activation for the given feature",
        description = """
                Returns the found activation.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully returned the activation",
            content = @Content(
                schema = @Schema(implementation = FeatureActivation.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request because of invalid keys",
            content = @Content(
                schema = @Schema(implementation = ServiceValidationErrorModel.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No activation exist with the given id",
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
    @GetMapping("/{activation_id}")
    @ResponseStatus(HttpStatus.OK)
    @Validated({Default.class, OnCrudRead.class})
    DeferredResult<FeatureActivation> getActivation(@Valid @PathVariable("feature_group_key") final String featureGroupKey,
                                                    @Valid @PathVariable("feature_key") final String featureKey,
                                                    @Valid @PathVariable("activation_id") final Long activationId);

    /**
     * Patches a specific activation for a given feature group.
     *
     * @param featureGroupKey the feature group key
     * @param featureKey      the feature key
     * @param activationId    the activation id
     * @param newActivation   the activation to patch
     * @return the activation after it has been patched.
     */
    @Operation(
        summary = "Patches a specific activation for a given feature",
        description = """
                Returns the patched activation.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully patched the activation",
            content = @Content(
                schema = @Schema(implementation = FeatureActivation.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request because of invalid feature group key, feature key or invalid body",
            content = @Content(
                schema = @Schema(implementation = ServiceValidationErrorModel.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No activation exist with the given id",
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
    @PatchMapping("/{activation_id}")
    @ResponseStatus(HttpStatus.OK)
    @Validated({Default.class, OnCrudUpdate.class})
    DeferredResult<FeatureActivation> patchActivation(
        @Valid @PathVariable("feature_group_key") final String featureGroupKey,
        @Valid @PathVariable("feature_key") final String featureKey,
        @Valid @PathVariable("activation_id") final Long activationId,
        @Valid @RequestBody FeatureActivation newActivation
    );

}
