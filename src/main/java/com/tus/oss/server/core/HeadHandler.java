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

/**
 * @author ckaratza
 * Return all the information regarding an upload.
 */
@Component
public class HeadHandler {

    private static final Logger log = LoggerFactory.getLogger(HeadHandler.class);

    private final String tusResumable;
    private final UploadManager uploadManager;

    @Inject
    public HeadHandler(@Value("${TusResumable}") String tusResumable, UploadManager uploadManager) {
        this.tusResumable = tusResumable;
        this.uploadManager = uploadManager;
    }

    @Operation(summary = "Provides status information for a specific upload.", method = "HEAD",
            responses = {
                    @ApiResponse(responseCode = "404", description = "Upload unit of work not found."),
                    @ApiResponse(responseCode = "200", description = "Upload unit of work found.",
                            headers = {@Header(name = "Upload-Length", description = "The total length of the upload unit of work.", required = true),
                                    @Header(name = "Upload-Offset", description = "How many bytes have been uploaded so far.", required = true)})},
            parameters = {@Parameter(in = ParameterIn.PATH, name = "uploadID",
                    required = true, description = "The ID of the upload unit of work", schema = @Schema(type = "string", format = "uuid"))})
    void handleRequest(RoutingContext ctx) {
        String uploadID = ctx.request().getParam("uploadID");
        HttpServerResponse response = ctx.response();
        uploadManager.findUploadInfo(uploadID).or(() -> {
            response.setStatusCode(404);
            return Optional.empty();
        }).ifPresent(info -> {
            response.putHeader("Cache-Control", "no-store");
            response.putHeader("Upload-Length", Long.toString(info.getEntityLength()));
            response.putHeader("Upload-Offset", Long.toString(info.getOffset()));
            if (info.getMetadata() != null) response.putHeader("Upload-Metadata", info.getMetadata());
            if (info.isPartial()) response.putHeader("Upload-Concat", "partial");
            else if (info.getUploadConcatMergedValue() != null)
                response.putHeader("Upload-Concat", info.getUploadConcatMergedValue());
            response.setStatusCode(200);
        });
        response.putHeader("Tus-Resumable", tusResumable);
        response.end();
    }
}
