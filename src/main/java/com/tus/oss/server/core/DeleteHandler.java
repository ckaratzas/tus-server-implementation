package com.tus.oss.server.core;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author ckaratza
 * The termination extension handler.
 */
@Component
public class DeleteHandler {

    private static final Logger log = LoggerFactory.getLogger(DeleteHandler.class);

    private final String tusResumable;
    private final UploadManager uploadManager;

    @Inject
    public DeleteHandler(@Value("${TusResumable}") String tusResumable, UploadManager uploadManager) {
        this.tusResumable = tusResumable;
        this.uploadManager = uploadManager;
    }

    @Operation(summary = "Deletes a specific upload.", method = "DELETE",
            responses = {@ApiResponse(responseCode = "204", description = "Request processed")},
            parameters = {@Parameter(in = ParameterIn.PATH, name = "uploadID",
                    required = true, description = "The ID of the upload unit of work",
                    schema = @Schema(type = "string", format= "uuid"))})
    void handleRequest(RoutingContext ctx) {
        String uploadID = ctx.request().getParam("uploadID");
        HttpServerResponse response = ctx.response();
        boolean deleted = uploadManager.discardUpload(uploadID);
        log.info("UploadID deleted {}.", deleted);
        response.setStatusCode(204);
        response.putHeader("Tus-Resumable", tusResumable);
        response.end();
    }
}
