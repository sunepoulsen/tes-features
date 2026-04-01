package dk.sunepoulsen.tes.features.service.domains.features.openapi;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag(name = "Feature Groups", description = "Endpoints to manage feature groups")
public @interface FeatureGroups {
}
