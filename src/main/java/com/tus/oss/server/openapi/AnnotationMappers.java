package com.tus.oss.server.openapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ckaratza
 * Simple OpenApi Annotation mapping to OpenApi models.
 */
final class AnnotationMappers {

    private static final Logger log = LoggerFactory.getLogger(AnnotationMappers.class);

    static void decorateOperationFromAnnotation(Operation annotation, io.swagger.v3.oas.models.Operation operation) {
        operation.summary(annotation.summary());
        operation.description(annotation.description());
        operation.operationId(annotation.operationId());
        operation.deprecated(annotation.deprecated());
        ApiResponses apiResponses = new ApiResponses();
        apiResponses.putAll(
                Arrays.stream(annotation.responses()).map(response -> {
                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.description(response.description());
                    Arrays.stream(response.headers()).forEach(header -> {
                        Header h = new Header();
                        h.description(header.description());
                        h.deprecated(header.deprecated());
                        h.allowEmptyValue(header.allowEmptyValue());
                        h.required(header.required());
                        apiResponse.addHeaderObject(header.name(), h);
                    });
                    return new ImmutablePair<>(response.responseCode(), apiResponse);
                }).collect(Collectors.toMap(x -> x.left, x -> x.right)));
        operation.responses(apiResponses);
        Arrays.stream(annotation.parameters()).forEach(parameter -> {
            Parameter p = findAlreadyProcessedParamFromVertxRoute(parameter.name(), operation.getParameters());
            if (p == null) {
                p = new Parameter();
                operation.addParametersItem(p);
            }
            p.name(parameter.name());
            p.description(parameter.description());
            p.allowEmptyValue(parameter.allowEmptyValue());
            try {
                p.style(Parameter.StyleEnum.valueOf(parameter.style().name()));
            } catch (IllegalArgumentException ie) {
                log.warn(ie.getMessage());
            }
            p.setRequired(parameter.required());
            p.in(parameter.in().name().toLowerCase());

            Schema schema = new Schema();
            io.swagger.v3.oas.annotations.media.Schema s = parameter.schema();
            if (!s.ref().isEmpty()) schema.set$ref(s.ref());
            schema.setDeprecated(s.deprecated());
            schema.setDescription(s.description());
            schema.setName(s.name());
            schema.setType(s.type());
            schema.setFormat(s.format());
            p.schema(schema);
        });
    }

    private static Parameter findAlreadyProcessedParamFromVertxRoute(final String name, List<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            if (name.equals(parameter.getName()))
                return parameter;
        }
        return null;
    }
}
