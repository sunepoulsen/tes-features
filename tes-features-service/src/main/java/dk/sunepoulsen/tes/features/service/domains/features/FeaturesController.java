package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.EnvelopeFeature;
import dk.sunepoulsen.tes.features.model.Feature;
import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup;
import dk.sunepoulsen.tes.springboot.rest.logic.async.DeferredResults;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.LogicException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping
class FeaturesController implements FeaturesOperations {

    private final FeaturesLogic featuresLogic;

    @Autowired
    public FeaturesController(FeaturesLogic featuresLogic) {
        this.featuresLogic = featuresLogic;
    }

    @Override
    @PutMapping(REGISTER_FEATURES_ENDPOINT_PATH)
    public DeferredResult<RegisterFeatureGroup> registerFeatures(RegisterFeatureGroup featureGroup) {
        try {
            return DeferredResults.of(featuresLogic.registerFeatures(featureGroup));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @GetMapping(FEATURES_ENDPOINT_PATH)
    public DeferredResult<EnvelopeFeature> getFeatures(@Valid @PathVariable("feature_group_key") final String key) {
        try {
            return DeferredResults.of(featuresLogic.getFeatures(key));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @GetMapping(FEATURE_ENDPOINT_PATH)
    public DeferredResult<Feature> getFeature(
        @Valid @PathVariable("feature_group_key") final String featureGroupKey,
        @Valid @PathVariable("feature_key") final String featureKey) {
        try {
            return DeferredResults.of(featuresLogic.getFeature(featureGroupKey, featureKey));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @PatchMapping(FEATURE_ENDPOINT_PATH)
    public DeferredResult<Feature> patchFeature(
        @Valid @PathVariable("feature_group_key") final String featureGroupKey,
        @Valid @PathVariable("feature_key") final String featureKey,
        @Valid @RequestBody final Feature feature) {
        try {
            return DeferredResults.of(featuresLogic.patchFeature(featureGroupKey, featureKey, feature));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

}
