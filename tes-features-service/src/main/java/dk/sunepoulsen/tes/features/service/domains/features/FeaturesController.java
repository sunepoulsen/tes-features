package dk.sunepoulsen.tes.features.service.domains.features;

import dk.sunepoulsen.tes.features.model.RegisterFeatureGroup;
import dk.sunepoulsen.tes.springboot.rest.logic.async.DeferredResults;
import dk.sunepoulsen.tes.springboot.rest.logic.exceptions.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/features")
class FeaturesController implements FeaturesOperations {

    private final FeaturesLogic featuresLogic;

    @Autowired
    public FeaturesController(FeaturesLogic featuresLogic) {
        this.featuresLogic = featuresLogic;
    }

    @Override
    @PutMapping
    public DeferredResult<RegisterFeatureGroup> registerFeatures(RegisterFeatureGroup featureGroup) {
        try {
            return DeferredResults.of(featuresLogic.registerFeatures(featureGroup));
        } catch (LogicException ex) {
            throw ex.mapApiException();
        }
    }

}
