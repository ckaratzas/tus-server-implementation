package com.tus.oss.server.core;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author ckaratza
 * The very simple options call that reveals Tus Server information.
 */
@Component
public class OptionsHandler {

    private final String tusResumable;
    private final String tusVersion;
    private final String tusMaxSize;
    private final String tusExtensions;
    private final String tusChecksumAlgorithms;

    @Inject
    public OptionsHandler(@Value("${TusResumable}") String tusResumable, @Value("${TusVersion}") String tusVersion,
                          @Value("${TusChecksumAlgorithms}") String tusChecksumAlgorithms,
                          @Value("${TusMaxSize}") String tusMaxSize, @Value("${TusExtensions}") String tusExtensions) {
        this.tusResumable = tusResumable;
        this.tusVersion = tusVersion;
        this.tusMaxSize = tusMaxSize;
        this.tusExtensions = tusExtensions;
        this.tusChecksumAlgorithms = tusChecksumAlgorithms;
    }

    @Operation(summary = "Provides information about the server implementation of the Tus.io protocol.", method = "OPTIONS",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Server Information.",
                            headers = {@Header(name = "Tus-Version", description = "The versions of Tus.io protocol supported.", required = true),
                                    @Header(name = "Tus-Max-Size", description = "The maximum length server allows to be uploaded.", required = true),
                                    @Header(name = "Tus-Extension", description = "Tus.io extensions currently supported.", required = true),
                                    @Header(name = "Tus-Checksum-Algorithm", description = "Tus.io checksum algorithms currently supported.", required = true)})})
    void handleRequest(RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        response.putHeader("Tus-Resumable", tusResumable);
        response.putHeader("Tus-Version", tusVersion);
        response.putHeader("Tus-Max-Size", tusMaxSize);
        response.putHeader("Tus-Extension", tusExtensions);
        response.putHeader("Tus-Checksum-Algorithm", tusChecksumAlgorithms);
        response.setStatusCode(204);
        response.end();
    }
}
