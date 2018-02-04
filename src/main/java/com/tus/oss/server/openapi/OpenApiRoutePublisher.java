package com.tus.oss.server.openapi;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.vertx.ext.web.Router;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author ckaratza
 * Exposes the OpenAPI spec as a vertx route.
 */
public final class OpenApiRoutePublisher {

    private final static Map<String, OpenAPI> generatedSpecs = new HashMap<>();

    public synchronized static void publishOpenApiSpec(Router router, String path, String title, String version, String serverUrl) {
        Optional<OpenAPI> spec = Optional.ofNullable(generatedSpecs.get(path)).or(() -> {
            OpenAPI openAPI = OpenApiSpecGenerator.generateOpenApiSpecFromRouter(router, title, version, serverUrl);
            generatedSpecs.put(path, openAPI);
            return Optional.of(openAPI);
        });
        if (spec.isPresent()) {
            router.get(path + ".json").handler(routingContext ->
                    routingContext.response()
                            .putHeader("Content-Type", "application/json")
                            .end(Json.pretty(spec.get())));
            router.get(path + ".yaml").handler(routingContext ->
                    routingContext.response()
                            .putHeader("Content-Type", "text/plain")
                            .end(Yaml.pretty(spec.get())));
        }
    }
}
