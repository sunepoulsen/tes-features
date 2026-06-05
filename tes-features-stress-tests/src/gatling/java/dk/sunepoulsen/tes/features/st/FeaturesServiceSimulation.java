package dk.sunepoulsen.tes.features.st;

import com.github.tomakehurst.wiremock.client.WireMock;
import dk.sunepoulsen.tes.data.generators.DataGenerator;
import dk.sunepoulsen.tes.data.generators.Generators;
import dk.sunepoulsen.tes.data.generators.NumberGenerators;
import dk.sunepoulsen.tes.data.generators.TimeGenerators;
import dk.sunepoulsen.tes.features.data.generators.FeatureActivationDataGenerator;
import dk.sunepoulsen.tes.features.data.generators.RegisterFeatureGroupDataGenerator;
import dk.sunepoulsen.tes.features.model.Feature;
import dk.sunepoulsen.tes.features.model.FeatureActivation;
import dk.sunepoulsen.tes.features.model.FeatureGroup;
import dk.sunepoulsen.tes.gatling.checks.GatlingChecks;
import dk.sunepoulsen.tes.gatling.scenarios.GatlingBodies;
import dk.sunepoulsen.tes.gatling.scenarios.KubernetesScenarios;
import dk.sunepoulsen.tes.keycloak.wiremock.KeycloakJwt;
import dk.sunepoulsen.tes.keycloak.wiremock.KeycloakJwtUser;
import dk.sunepoulsen.tes.keycloak.wiremock.KeycloakWiremock;
import dk.sunepoulsen.tes.lang.SystemEnvironment;
import dk.sunepoulsen.tes.wiremock.deployment.WiremockServerEnvironment;
import dk.sunepoulsen.tes.wiremock.deployment.WiremockServerProperties;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class FeaturesServiceSimulation extends Simulation {

    private static final Duration RAMP_USERS_DURATION = Duration.ofSeconds(10);
    private static final Duration HOLD_USERS_DURATION = Duration.ofSeconds(30);
    private static final Duration SIMULATION_DURATION = Duration.ofSeconds(50);
    private static final int INIT_USERS = 1;
    private static final int MAX_USERS = 50;

    private static final Duration PAUSE_REST_CALLS_DURATION = Duration.ofMillis(100);
    private static KeycloakJwt keycloakJwt = null;

    private final DataGenerator<String> textGenerator = Generators.textGenerator(NumberGenerators.integerGenerator(10, 50));

    private static String featuresBaseUrl() {
        return SystemEnvironment.readVariable("FEATURES_SERVICE_URL");
    }

    private final HttpProtocolBuilder httpProtocol =
        http.baseUrl(featuresBaseUrl())
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .header("Authorization", session -> {
                final String token = keycloakJwt.createAuthorizationToken(UUID.randomUUID().toString(), "tes-foundation", KeycloakJwtUser.builder()
                    .clientId("tes-foundation")
                    .username("jennifer")
                    .roles(List.of("register-features", "admin-features"))
                    .build()
                );

                return "Bearer %s".formatted(token);
            });

    private final ScenarioBuilder registerFeaturesScenario =
        scenario("register-features")
            .exec(
                http("register-feature-group")
                    .put("/features")
                    .body(GatlingBodies.jsonBody(new RegisterFeatureGroupDataGenerator()))
                    .check(status().is(200))
                    .check(jsonPath("$.key").saveAs("featureGroupKey"))
                    .check(jsonPath("$.activations[0].id").saveAs("featureGroupActivationId"))
                    .check(jsonPath("$.features[0].key").saveAs("featureKey"))
                    .check(jsonPath("$.features[0].activations[0].id").saveAs("featureActivationId"))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(
                http("list-feature-groups")
                    .get("/groups")
                    .check(status().is(200))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(
                http("get-feature-group")
                    .get("/groups/#{featureGroupKey}")
                    .check(status().is(200))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(session -> {
                FeatureGroup model = new FeatureGroup();
                model.setName(textGenerator.generate());

                return session
                    .set("patchFeatureGroup", model)
                    .set("patchFeatureGroupNewName", model.getName());
            })
            .exec(
                http("patch-feature-group")
                    .patch("/groups/#{featureGroupKey}")
                    .body(GatlingBodies.jsonSessionPropertyBody("patchFeatureGroup"))
                    .check(status().is(200))
                    .check(jsonPath("$.name").saveAs("patchedFeatureGroupName"))
            )
            .exec(GatlingChecks.sessionPropertyEquals(
                "Patched Feature Group Name",
                "patchedFeatureGroupName",
                "patchFeatureGroupNewName")
            )
            .exec(
                http("post-feature-group-activation")
                    .post("/groups/#{featureGroupKey}/activations")
                    .body(GatlingBodies.jsonBody(new FeatureActivationDataGenerator()))
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("newFeatureGroupActivationId"))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(
                http("list-feature-group-activations")
                    .get("/groups/#{featureGroupKey}/activations")
                    .check(status().is(200))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(
                http("get-feature-group-activation")
                    .get("/groups/#{featureGroupKey}/activations/#{featureGroupActivationId}")
                    .check(status().is(200))
                    .check(jsonPath("$.id").isEL("#{featureGroupActivationId}"))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(session -> {
                FeatureActivation model = new FeatureActivation();
                model.setDatetime(TimeGenerators.currentZonedDateTimeGenerator(ZoneId.of("Z")).generate().plusHours(5));

                return session
                    .set("patchFeatureGroupActivation", model)
                    .set("patchFeatureGroupActivationNewDatetime", model.getDatetime().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            })
            .exec(
                http("patch-feature-group-activation")
                    .patch("/groups/#{featureGroupKey}/activations/#{featureGroupActivationId}")
                    .body(GatlingBodies.jsonSessionPropertyBody("patchFeatureGroupActivation"))
                    .check(status().is(200))
                    .check(jsonPath("$.datetime").saveAs("patchFeatureGroupActivationDatetime"))
            )
            .exec(GatlingChecks.sessionPropertyEquals(
                "Patched Feature Group Activation Datetime",
                "patchFeatureGroupActivationDatetime",
                "patchFeatureGroupActivationNewDatetime")
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(
                http("delete-feature-group-activation")
                    .delete("/groups/#{featureGroupKey}/activations/#{featureGroupActivationId}")
                    .check(status().is(204))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(
                http("list-features")
                    .get("/groups/#{featureGroupKey}/features")
                    .check(status().is(200))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(
                http("get-feature")
                    .get("/groups/#{featureGroupKey}/features/#{featureKey}")
                    .check(status().is(200))
                    .check(jsonPath("$.key").isEL("#{featureKey}"))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(session -> {
                Feature model = new Feature();
                model.setName(textGenerator.generate());

                return session
                    .set("patchFeature", model)
                    .set("patchFeatureNewName", model.getName());
            })
            .exec(
                http("patch-feature")
                    .patch("/groups/#{featureGroupKey}/features/#{featureKey}")
                    .body(GatlingBodies.jsonSessionPropertyBody("patchFeature"))
                    .check(status().is(200))
                    .check(jsonPath("$.name").saveAs("patchedFeatureName"))
            )
            .exec(GatlingChecks.sessionPropertyEquals(
                "Patched Feature Name",
                "patchedFeatureName",
                "patchFeatureNewName")
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(
                http("post-feature-activation")
                    .post("/groups/#{featureGroupKey}/features/#{featureKey}/activations")
                    .body(GatlingBodies.jsonBody(new FeatureActivationDataGenerator()))
                    .check(status().is(201))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(
                http("list-feature-activations")
                    .get("/groups/#{featureGroupKey}/features/#{featureKey}/activations")
                    .check(status().is(200))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(
                http("get-feature-activation")
                    .get("/groups/#{featureGroupKey}/features/#{featureKey}/activations/#{featureActivationId}")
                    .check(status().is(200))
                    .check(jsonPath("$.id").isEL("#{featureActivationId}"))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(session -> {
                FeatureActivation model = new FeatureActivation();
                model.setDatetime(TimeGenerators.currentZonedDateTimeGenerator(ZoneId.of("Z")).generate().plusHours(5));

                return session
                    .set("patchFeatureActivation", model)
                    .set("patchFeatureActivationNewDatetime", model.getDatetime().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            })
            .exec(
                http("patch-feature-activation")
                    .patch("/groups/#{featureGroupKey}/features/#{featureKey}/activations/#{featureActivationId}")
                    .body(GatlingBodies.jsonSessionPropertyBody("patchFeatureActivation"))
                    .check(status().is(200))
                    .check(jsonPath("$.datetime").saveAs("patchFeatureActivationDatetime"))
            )
            .exec(GatlingChecks.sessionPropertyEquals(
                "Patched Feature Activation Datetime",
                "patchFeatureActivationDatetime",
                "patchFeatureActivationNewDatetime")
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(
                http("delete-feature-activation")
                    .delete("/groups/#{featureGroupKey}/features/#{featureKey}/activations/#{featureActivationId}")
                    .check(status().is(204))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(
                http("delete-feature")
                    .delete("/groups/#{featureGroupKey}/features/#{featureKey}")
                    .check(status().is(204))
            )
            .pause(PAUSE_REST_CALLS_DURATION)
            .exec(
                http("delete-feature-group")
                    .delete("/groups/#{featureGroupKey}")
                    .check(status().is(204))
            );

    public FeaturesServiceSimulation() {
        KubernetesScenarios kubernetesScenarios = new KubernetesScenarios(featuresBaseUrl());

        List<PopulationBuilder> populationBuilders = new ArrayList<>(kubernetesScenarios.populate(SIMULATION_DURATION));
        populationBuilders.add(
            registerFeaturesScenario.injectClosed(
                    rampConcurrentUsers(INIT_USERS).to(MAX_USERS).during(RAMP_USERS_DURATION),
                    constantConcurrentUsers(MAX_USERS).during(HOLD_USERS_DURATION)
                )
                .protocols(httpProtocol)
        );

        setUp(populationBuilders)
            .assertions(
                global().failedRequests().percent().lt(1.0),
                global().responseTime().percentile4().lt(20000)
            )
            .maxDuration(SIMULATION_DURATION);
    }

    @Override
    public void before() {
        super.before();

        WiremockServerProperties wiremockServerProperties = WiremockServerEnvironment.constructFromEnvironmentVariables(System.getenv());
        WireMock.configureFor(wiremockServerProperties.getExternalHost(), wiremockServerProperties.getExternalPort());

        KeycloakWiremock keycloakWiremock = new KeycloakWiremock(wiremockServerProperties, "keycloak", "tes-foundation", "tes-foundation-kid");
        keycloakWiremock.createStubs();

        keycloakJwt = keycloakWiremock.keycloakJwt();
    }

}
