package dk.sunepoulsen.tes.features.ct;

import dk.sunepoulsen.tes.deployment.core.steps.factories.ConfigurationFileStepsResult;
import dk.sunepoulsen.tes.features.deployment.FeaturesDeployment;
import dk.sunepoulsen.tes.flows.FlowStep;
import dk.sunepoulsen.tes.postgres.steps.factories.PostgresConfigureStepsDatabaseResult;
import dk.sunepoulsen.tes.postgres.steps.factories.PostgresConfigureStepsDatabasesResult;
import dk.sunepoulsen.tes.sut.engine.exceptions.DeploymentException;
import dk.sunepoulsen.tes.sut.engine.extensions.AbstractSystemUnderTestExtension;
import dk.sunepoulsen.tes.sut.engine.steps.SutStartTesServiceStep;
import dk.sunepoulsen.tes.sut.engine.steps.factories.ContainerStepResult;
import dk.sunepoulsen.tes.sut.engine.system.SystemUnderTestDeployment;
import dk.sunepoulsen.tes.sut.postgres.steps.SutStartPostgresStep;
import dk.sunepoulsen.tes.sut.postgres.steps.factories.PostgresContainerStepsFactory;
import dk.sunepoulsen.tes.wiremock.deployment.steps.SutStartWiremockStep;
import dk.sunepoulsen.tes.wiremock.deployment.steps.factories.WiremockContainerStepsFactory;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DeploymentSpockExtension extends AbstractSystemUnderTestExtension {

    private final Path featuresDirectory;

    public DeploymentSpockExtension() {
        super();
        this.featuresDirectory = FileSystems.getDefault().getPath(deployDirectory.toAbsolutePath().toString(), "features");
    }

    @Override
    protected List<FlowStep> configureDeploySteps(SystemUnderTestDeployment systemUnderTestDeployment) {
        try {
            log.info("Configuring deployment steps");

            FeaturesDeployment featuresDeployment = new FeaturesDeployment(List.of("ct", "tests"));
            featuresDeployment.setConfigTemplateName("templates/application-ct.yml");

            WiremockContainerStepsFactory wiremockContainerStepsFactory = new WiremockContainerStepsFactory(systemUnderTestDeployment, networkStep, logDirectory);
            ContainerStepResult<SutStartWiremockStep> wiremockContainerSteps = wiremockContainerStepsFactory.createSteps();

            PostgresConfigureStepsDatabaseResult featuresConfigureDatabaseSteps = featuresDeployment.configureDatabaseSteps(deployDirectory);

            PostgresContainerStepsFactory postgresContainerStepsFactory = new PostgresContainerStepsFactory(systemUnderTestDeployment, networkStep, logDirectory);
            ContainerStepResult<SutStartPostgresStep> postgresContainerSteps = postgresContainerStepsFactory.createSteps(new PostgresConfigureStepsDatabasesResult(featuresConfigureDatabaseSteps));

            ConfigurationFileStepsResult featuresConfigSteps = featuresDeployment.configurationSteps(certificateStepsResult, featuresConfigureDatabaseSteps, postgresContainerSteps.getStep().getAliases().getFirst(), featuresDirectory);
            ContainerStepResult<SutStartTesServiceStep> featuresContainerSteps = featuresDeployment.containerSteps(systemUnderTestDeployment, networkStep, certificateStepsResult, featuresConfigSteps, logDirectory);

            log.info("Returning configured deployment steps");
            List<FlowStep> steps = new ArrayList<>();
            steps.addAll(wiremockContainerSteps.steps());
            steps.addAll(featuresConfigureDatabaseSteps.steps());
            steps.addAll(postgresContainerSteps.steps());
            steps.addAll(featuresConfigSteps.steps());
            steps.addAll(featuresContainerSteps.steps());

            return steps;
        } catch (Exception ex) {
            log.info("Failed to create deployment steps", ex);
            throw new DeploymentException("Failed to create deployment steps", ex);
        }
    }

}
