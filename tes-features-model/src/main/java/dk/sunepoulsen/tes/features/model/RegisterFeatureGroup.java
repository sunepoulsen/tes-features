package dk.sunepoulsen.tes.features.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "Feature group", description = "Defines a group of features")
public class RegisterFeatureGroup extends RegisterFeature {
    @Schema(
        description = "Features in this feature group",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        accessMode = Schema.AccessMode.READ_WRITE
    )
    @Valid
    private List<RegisterFeature> features;
}
