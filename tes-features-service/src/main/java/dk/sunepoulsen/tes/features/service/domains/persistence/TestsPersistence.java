package dk.sunepoulsen.tes.features.service.domains.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestsPersistence {

    private final FeatureGroupRepository featureGroupRepository;
    private final FeatureRepository featureRepository;

    public void clearDatabase() {
        featureRepository.deleteAll();
        featureGroupRepository.deleteAll();
    }

}
