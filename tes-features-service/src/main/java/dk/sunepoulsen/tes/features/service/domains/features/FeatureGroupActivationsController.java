package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureActivation;
import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.springboot.rest.logic.async.DeferredResults;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.LogicException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public DeferredResult<FeatureActivation> createActivation(String key, FeatureActivation newActivation) {
        try {
            return DeferredResults.of(featureGroupsLogic.createActivation(key, newActivation));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @GetMapping
    public DeferredResult<EnvelopeFeatureActivation> getActivations(String key) {
        try {
            return DeferredResults.of(featureGroupsLogic.getActivations(key));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }
}
