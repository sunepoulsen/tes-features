package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.EnvelopeFeature;
import dk.sunepoulsen.tes.features.model.Feature;
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup;
import dk.sunepoulsen.tes.rest.models.EnvelopeModel;
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel;
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel;
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudCreate;
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@Tag(name = "Features", description = "Endpoints to manage features")
@RequestMapping
@Validated
public interface FeaturesOperations {
    String REGISTER_FEATURES_ENDPOINT_PATH = "/features";
    String FEATURES_ENDPOINT_PATH = FeatureGroupsOperations.FEATURE_GROUPS_ENDPOINT_PATH + "/{feature_group_key}/features";
    String FEATURE_ENDPOINT_PATH = FeatureGroupsOperations.FEATURE_GROUPS_ENDPOINT_PATH + "/{feature_group_key}/features/{feature_key}";

    @Operation(
        summary = "Register features within a given feature group",
        description = """
                Register a set of features
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully created or ignored if the feature already exists",
            content = @Content(
                schema = @Schema(implementation = RegisterFeatureGroup.class)
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
            responseCode = "500",
            description = "Unable to process this request",
            content = @Content(
                schema = @Schema(implementation = ServiceErrorModel.class)
            )
        )
    })
    @PutMapping(REGISTER_FEATURES_ENDPOINT_PATH)
    @ResponseStatus(HttpStatus.OK)
    @Validated({Default.class, OnCrudCreate.class})
    DeferredResult<RegisterFeatureGroup> registerFeatures(@Valid @RequestBody RegisterFeatureGroup featureGroup);

    @Operation(
        summary = "Returns a list of all registered feature groups",
        description = """
                Returns all found feature groups.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully returned all found features groups",
            content = @Content(
                schema = @Schema(implementation = EnvelopeModel.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request because of invalid feature group key",
            content = @Content(
                schema = @Schema(implementation = ServiceValidationErrorModel.class)
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
    @GetMapping(FEATURES_ENDPOINT_PATH)
    @ResponseStatus(HttpStatus.OK)
    @Validated({Default.class, OnCrudRead.class})
    DeferredResult<EnvelopeFeature> getFeatures(@Valid @PathVariable("feature_group_key") final String key);

    @Operation(
        summary = "Returns the feature with the given keys",
        description = """
                Returns a feature.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully returned a found feature",
            content = @Content(
                schema = @Schema(implementation = Feature.class)
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
            description = "No feature could be found",
            content = @Content(
                schema = @Schema(implementation = ServiceValidationErrorModel.class)
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
    @GetMapping(FEATURE_ENDPOINT_PATH)
    @ResponseStatus(HttpStatus.OK)
    @Validated({Default.class, OnCrudRead.class})
    DeferredResult<Feature> getFeature(
        @Valid @PathVariable("feature_group_key") final String featureGroupKey,
        @Valid @PathVariable("feature_key") final String featureKey);
}
