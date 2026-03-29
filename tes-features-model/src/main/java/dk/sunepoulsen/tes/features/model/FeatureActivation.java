package dk.sunepoulsen.tes.features.model;

import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudCreate;
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudRead;
import dk.sunepoulsen.tes.rest.models.validation.annotations.OnCrudUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Schema(name = "Feature activation", description = "Defines an activation of a feature group or feature")
public class FeatureActivation {
    @Schema(
        description = "Unique id of the feature activation",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_ONLY
    )
    @Null(groups = {OnCrudCreate.class, OnCrudUpdate.class})
    @NotNull(groups = {OnCrudRead.class})
    private Long id;

    @Schema(
        description = "Set whether the feature or feature is enabled or not",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    @NotNull(groups = {OnCrudCreate.class, OnCrudRead.class})
    private Boolean enabled;

    @Schema(
        description = "Date and time of when the feature or feature group is enabled or disabled",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    @NotNull(groups = {OnCrudCreate.class, OnCrudRead.class})
    private ZonedDateTime datetime;
}
