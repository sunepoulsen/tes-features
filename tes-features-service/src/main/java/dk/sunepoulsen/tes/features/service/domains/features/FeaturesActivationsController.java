package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.springboot.rest.logic.async.DeferredResults;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.LogicException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping(FeaturesActivationOperations.ENDPOINT_PATH)
@RequiredArgsConstructor
class FeaturesActivationsController implements FeaturesActivationOperations {

    private final FeaturesLogic featuresLogic;

    @Override
    @PostMapping
    public DeferredResult<FeatureActivation> createActivation(String featureGroupKey, String featureKey, FeatureActivation newActivation) {
        try {
            return DeferredResults.of(featuresLogic.createActivation(featureGroupKey, featureKey, newActivation));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }
}
