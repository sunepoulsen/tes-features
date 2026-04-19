package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.EnvelopeFeatureGroup;
import dk.sunepoulsen.tes.features.model.FeatureGroup;
import dk.sunepoulsen.tes.springboot.rest.logic.async.DeferredResults;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.LogicException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping(FeatureGroupsOperations.FEATURE_GROUPS_ENDPOINT_PATH)
class FeatureGroupsController implements FeatureGroupsOperations {

    private final FeatureGroupsLogic featureGroupsLogic;

    @Autowired
    public FeatureGroupsController(FeatureGroupsLogic featureGroupsLogic) {
        this.featureGroupsLogic = featureGroupsLogic;
    }

    @Override
    @GetMapping
    public DeferredResult<EnvelopeFeatureGroup> getFeatureGroups() {
        try {
            return DeferredResults.of(featureGroupsLogic.getFeatureGroups());
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @GetMapping("/{feature_group_key}")
    public DeferredResult<FeatureGroup> getFeatureGroup(@Valid @PathVariable("feature_group_key") final String key) {
        try {
            return DeferredResults.of(featureGroupsLogic.getFeatureGroup(key));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @PatchMapping("/{feature_group_key}")
    public DeferredResult<FeatureGroup> patchFeatureGroup(@Valid @PathVariable("feature_group_key") final String key, @Valid @RequestBody FeatureGroup featureGroup) {
        try {
            return DeferredResults.of(featureGroupsLogic.patchFeatureGroup(key, featureGroup));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

    @Override
    @DeleteMapping("/{feature_group_key}")
    public DeferredResult<Void> deleteFeatureGroup(@Valid @PathVariable("feature_group_key") final String key) {
        try {
            return DeferredResults.of(featureGroupsLogic.deleteFeatureGroup(key));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

}
