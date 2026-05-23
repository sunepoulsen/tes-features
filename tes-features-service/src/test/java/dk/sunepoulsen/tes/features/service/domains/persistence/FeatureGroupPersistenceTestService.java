package dk.sunepoulsen.tes.features.service.domains.persistence;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureGroupPersistenceTestService {

    private final FeatureGroupRepository featureGroupRepository;
    private final FeatureRepository featureRepository;
    private final FeatureGroupActivationRepository featureGroupActivationRepository;
    private final FeatureActivationRepository featureActivationRepository;

    private List<CrudRepository<?, ?>> repositories;

    @PostConstruct
    public void init() {
        this.repositories = List.of(
            featureActivationRepository,
            featureRepository,
            featureGroupActivationRepository,
            featureGroupRepository
        );
    }

    void deleteAll() {
        log.debug("Deleting all rows in database");

        try {
            this.repositories.forEach(CrudRepository::deleteAll);
        } catch (Exception ex) {
            log.debug("Error deleting all rows in database", ex);
            throw ex;
        }
    }

}
