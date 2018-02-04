package com.tus.oss.server.core;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Optional;

import static com.tus.oss.server.core.Utils.*;

/**
 * @author ckaratza
 * The creation and concatenation extension implementation.
 */
@Component
public class PostHandler {

    private static final Logger log = LoggerFactory.getLogger(PostHandler.class);

    private final String tusResumable;
    private final UploadManager uploadManager;

    @Inject
    public PostHandler(@Value("${TusResumable}") String tusResumable, UploadManager uploadManager) {
        this.tusResumable = tusResumable;
        this.uploadManager = uploadManager;
    }


    @Operation(summary = "Creates a new upload unit-of-work.", method = "POST",
            parameters = {@Parameter(name = "Upload-Length", in = ParameterIn.HEADER, required = true),
                    @Parameter(name = "Upload-Concat", in = ParameterIn.HEADER, schema = @Schema(type = "string")),
                    @Parameter(name = "Upload-Metadata", in = ParameterIn.HEADER, schema = @Schema(type = "string"))},
            responses = {
                    @ApiResponse(responseCode = "413", description = "Upload size too large."),
                    @ApiResponse(responseCode = "400", description = "Bad Request."),
                    @ApiResponse(responseCode = "201", description = "Upload unit of work Created.",
                            headers = {@Header(name = "Location", description = "The uri of the created upload unit of work.", required = true)})})
    void handleRequest(RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        Optional<Long> lengthHeader = getHeaderAsLong("Upload-Length", ctx);
        Optional<String> uploadConcatHeader = getHeaderAsString("Upload-Concat", ctx);
        Optional<String> uploadMetadataHeader = getHeaderAsString("Upload-Metadata", ctx);
        boolean isPartial = "partial".equals(uploadConcatHeader.orElse(""));
        boolean isPotentiallyFinal = uploadConcatHeader.orElse("").startsWith("final;");
        if (isPotentiallyFinal) {
            log.info("Final Upload-Concat {}.", uploadConcatHeader.get());
            String[] parts = uploadConcatHeader.get().substring("final;".length() + 1).split(" ");
            if (parts.length <= 1) {
                response.setStatusCode(400);
            } else {
                Optional<String> location = uploadManager.mergePartialUploads(extractPartialUploadIds(parts), uploadMetadataHeader);
                if (location.isPresent()) {
                    response.putHeader("Location", location.get());
                    response.setStatusCode(201);
                } else {
                    response.setStatusCode(500);
                }
            }
        } else if (lengthHeader.isPresent()) {
            if (uploadManager.checkServerSizeConstraint(lengthHeader.get())) {
                Optional<String> location = uploadManager.createUpload(lengthHeader.get(),
                        uploadMetadataHeader, isPartial);
                if (location.isPresent()) {
                    response.putHeader("Location", location.get());
                    response.setStatusCode(201);
                } else {
                    response.setStatusCode(500);
                }
            } else {
                response.setStatusCode(413);
            }
        } else {
            response.setStatusCode(400);
        }
        response.putHeader("Tus-Resumable", tusResumable);
        response.end();
    }
}
