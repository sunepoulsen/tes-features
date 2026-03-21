package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureGroup;
import dk.sunepoulsen.tes.features.model.FeatureGroup;
import dk.sunepoulsen.tes.rest.models.EnvelopeModel;
import dk.sunepoulsen.tes.rest.models.ServiceErrorModel;
import dk.sunepoulsen.tes.rest.models.ServiceValidationErrorModel;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.DeferredResult;

@Tag(name = "Feature Groups", description = "Endpoints to manage registerFeature groups")
@RequestMapping(FeatureGroupsOperations.FEATURE_GROUPS_ENDPOINT_PATH)
@Validated
public interface FeatureGroupsOperations {
    String FEATURE_GROUPS_ENDPOINT_PATH = "/groups";

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
    DeferredResult<EnvelopeFeatureGroup> getFeatureGroups();

    @Operation(
        summary = "Returns the register feature group with a given key",
        description = """
                Returns the found register feature group excluding all features and activations that has been registered within the feature group.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully returned the found feature group",
            content = @Content(
                schema = @Schema(implementation = FeatureGroup.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "If feature group key contains invalid characters",
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
    @GetMapping("/{feature_group_key}")
    @ResponseStatus(HttpStatus.OK)
    @Validated({Default.class, OnCrudRead.class})
    DeferredResult<FeatureGroup> getFeatureGroup(@Valid @PathVariable("feature_group_key") final String key);
}
