package dk.sunepoulsen.tes.features.deployment;

import dk.sunepoulsen.tes.deployment.core.function.AtomicDataSupplier;
import dk.sunepoulsen.tes.deployment.core.steps.factories.*;
import dk.sunepoulsen.tes.io.resources.PropertiesResource;
import dk.sunepoulsen.tes.io.resources.ResourceException;
import dk.sunepoulsen.tes.sut.engine.steps.SutCreateTestContainerNetworkStep;
import dk.sunepoulsen.tes.sut.engine.steps.SutStartTesServiceStep;
import dk.sunepoulsen.tes.sut.engine.steps.factories.ContainerStepResult;
import dk.sunepoulsen.tes.sut.engine.steps.factories.TesContainerStepsFactory;
import dk.sunepoulsen.tes.sut.engine.system.SystemUnderTestDeployment;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.List;

public class FeaturesDeployment {

    private final List<String> profiles;

    @Getter
    @Setter
    private String configTemplateName;

    private final PropertiesResource propertiesResource;

    public FeaturesDeployment(String profile) throws ResourceException {
        this(List.of(profile));
    }

    public FeaturesDeployment(List<String> profiles) throws ResourceException {
        this.profiles = profiles;
        this.configTemplateName = "templates/application.yml";
        this.propertiesResource = new PropertiesResource(FeaturesDeployment.class.getResourceAsStream("/features-deployment.properties"));
    }

    public PostgresConfigureStepsDatabaseResult configureDatabaseSteps(Path deployDirectory) {
        PostgresConfigureStepsFactory postgresStepsFactory = new PostgresConfigureStepsFactory(deployDirectory);
        return postgresStepsFactory.createSteps("createFeaturesDatabase", "features");
    }

    public ConfigurationFileStepsResult configurationSteps(CertificateStepsResult certificateStepsResult, PostgresConfigureStepsDatabaseResult featuresDatabaseSteps, AtomicDataSupplier<String> databaseHost, Path storeDirectory) {
        ConfigurationFileStepsFactory configurationFileStepsFactory = new ConfigurationFileStepsFactory(configTemplateName, storeDirectory, "application-" + profiles.get(0) + ".yml");
        configurationFileStepsFactory.addDefaultTesServiceContext();
        configurationFileStepsFactory.addCertificateContext(certificateStepsResult);
        configurationFileStepsFactory.addDatabaseContext(databaseHost, featuresDatabaseSteps);

        return configurationFileStepsFactory.createSteps("featuresConfig");
    }

    public ContainerStepResult<SutStartTesServiceStep> containerSteps(SystemUnderTestDeployment systemUnderTestDeployment, SutCreateTestContainerNetworkStep networkStep, CertificateStepsResult certificateStepsResult, ConfigurationFileStepsResult configurationSteps, Path logDirectory) {
        TesContainerStepsFactory featuresServiceStepsFactory = new TesContainerStepsFactory(propertiesResource.getProperty("docker.image.name"), propertiesResource.getProperty("docker.image.tag"), systemUnderTestDeployment, networkStep, logDirectory);
        return featuresServiceStepsFactory.createSteps(profiles, propertiesResource.getProperty("features.service.key"), certificateStepsResult, configurationSteps);
    }
}
