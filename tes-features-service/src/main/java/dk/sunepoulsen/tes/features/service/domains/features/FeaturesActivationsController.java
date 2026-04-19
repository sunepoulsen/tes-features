package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureActivation;
import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.springboot.rest.logic.async.DeferredResults;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.LogicException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Controller for feature activations.
 */
@RestController
@RequestMapping(FeaturesActivationOperations.ENDPOINT_PATH)
@RequiredArgsConstructor
class FeaturesActivationsController implements FeaturesActivationOperations {

    private final FeaturesLogic featuresLogic;

    @Override
    @PostMapping
    public DeferredResult<FeatureActivation> createActivation(@Valid @PathVariable("feature_group_key") final String featureGroupKey,
                                                              @Valid @PathVariable("feature_key") final String featureKey,
                                                              @Valid @RequestBody FeatureActivation newActivation) {
        try {
            return DeferredResults.of(featuresLogic.createActivation(featureGroupKey, featureKey, newActivation));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @GetMapping
    public DeferredResult<EnvelopeFeatureActivation> getActivations(@Valid @PathVariable("feature_group_key") final String featureGroupKey,
                                                                    @Valid @PathVariable("feature_key") final String featureKey) {
        try {
            return DeferredResults.of(featuresLogic.getActivations(featureGroupKey, featureKey));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @GetMapping("/{activation_id}")
    public DeferredResult<FeatureActivation> getActivation(@Valid @PathVariable("feature_group_key") final String featureGroupKey,
                                                           @Valid @PathVariable("feature_key") final String featureKey,
                                                           @Valid @PathVariable("activation_id") final Long activationId) {
        try {
            return DeferredResults.of(featuresLogic.getActivation(featureGroupKey, featureKey, activationId));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @PatchMapping("/{activation_id}")
    public DeferredResult<FeatureActivation> patchActivation(
        @Valid @PathVariable("feature_group_key") final String featureGroupKey,
        @Valid @PathVariable("feature_key") final String featureKey,
        @Valid @PathVariable("activation_id") final Long activationId,
        @Valid @RequestBody FeatureActivation newActivation) {
        try {
            return DeferredResults.of(featuresLogic.patchActivation(featureGroupKey, featureKey, activationId, newActivation));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @DeleteMapping("/{activation_id}")
    public DeferredResult<Void> deleteActivation(
        @Valid @PathVariable("feature_group_key") final String featureGroupKey,
        @Valid @PathVariable("feature_key") final String featureKey,
        @Valid @PathVariable("activation_id") final Long activationId) {
        try {
            return DeferredResults.of(featuresLogic.deleteActivation(featureGroupKey, featureKey, activationId));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

}
