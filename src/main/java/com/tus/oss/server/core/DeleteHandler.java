package com.tus.oss.server.core;

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
