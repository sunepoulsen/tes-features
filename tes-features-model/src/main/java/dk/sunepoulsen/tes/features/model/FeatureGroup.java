package dk.sunepoulsen.tes.features.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dk.sunepoulsen.tes.rest.models.validation.constraints.UriPathPattern;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "Feature group", description = "Defines the basic information about a feature group")
@JsonPropertyOrder({"key", "name", "description"})
public class FeatureGroup {
    @Schema(
        description = "Unique key of this feature group",
        requiredMode = Schema.RequiredMode.REQUIRED,
        accessMode = Schema.AccessMode.READ_ONLY
    )
    @NotNull
    @UriPathPattern
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
}
