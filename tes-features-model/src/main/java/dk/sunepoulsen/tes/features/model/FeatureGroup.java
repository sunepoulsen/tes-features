package dk.sunepoulsen.tes.features.model;

import dk.sunepoulsen.tes.rest.models.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "Feature group", description = "Defines a group of features")
public class FeatureGroup implements BaseModel {
    @Schema(
        description = "Unique key of this feature group",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_ONLY
    )
    @NotNull
    private String key;

    @Schema(
        description = "Name of this feature group",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    @NotNull
    private String name;

    @Schema(
        description = "Description of this feature group",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    private String description;

    @Schema(
        description = "Features in this feature group",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    @Valid
    private List<Feature> features;

    @Schema(
        description = "Activations of this feature group",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    @Valid
    private List<FeatureActivation> activations;
}
