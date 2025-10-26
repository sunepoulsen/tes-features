package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.FeatureGroup;
import dk.sunepoulsen.tes.springboot.rest.logic.async.DeferredResults;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.LogicException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    @GetMapping("/{feature_group_key}")
    public DeferredResult<FeatureGroup> getFeatureGroup(@Valid @PathVariable("feature_group_key") final String key) {
        try {
            return DeferredResults.of(featureGroupsLogic.getFeatureGroup(key));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }
}
