package dk.sunepoulsen.tes.features.model;

import dk.sunepoulsen.tes.rest.models.EnvelopeModel;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Envelope model for feature activations.
 */
@Schema(name = "EnvelopeFeatureActivation")
public class EnvelopeFeatureActivation extends EnvelopeModel<FeatureActivation> {
}
