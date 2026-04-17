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
 * Controller for feature group activations.
 */
@RestController
@RequestMapping(FeatureGroupActivationOperations.ENDPOINT_PATH)
@RequiredArgsConstructor
public class FeatureGroupActivationsController implements FeatureGroupActivationOperations {

    private final FeatureGroupsLogic featureGroupsLogic;

    @Override
    @PostMapping
    public DeferredResult<FeatureActivation> createActivation(@Valid @PathVariable("feature_group_key") final String key,
                                                              @Valid @RequestBody FeatureActivation newActivation) {
        try {
            return DeferredResults.of(featureGroupsLogic.createActivation(key, newActivation));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @GetMapping
    public DeferredResult<EnvelopeFeatureActivation> getActivations(@Valid @PathVariable("feature_group_key") final String key) {
        try {
            return DeferredResults.of(featureGroupsLogic.getActivations(key));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @GetMapping("/{activation_id}")
    public DeferredResult<FeatureActivation> getActivation(@Valid @PathVariable("feature_group_key") final String key,
                                                           @Valid @PathVariable("activation_id") final Long activationId) {
        try {
            return DeferredResults.of(featureGroupsLogic.getActivation(key, activationId));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @PatchMapping("/{activation_id}")
    public DeferredResult<FeatureActivation> patchActivation(
        @Valid @PathVariable("feature_group_key") final String key,
        @Valid @PathVariable("activation_id") final Long activationId,
        @Valid @RequestBody FeatureActivation newActivation) {
        try {
            return DeferredResults.of(featureGroupsLogic.patchActivation(key, activationId, newActivation));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

}
