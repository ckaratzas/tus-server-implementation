package com.tus.oss.server.core;

import io.vertx.core.Vertx;
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
 * The patch method handling that performs sanity checks on the request and delegates to a storage plugin.
 * The storage call is executed in a blocking context in order not to block vertx event loop.
 * It also handles checksum extension.
 */
@Component
public class PatchHandler {

    private static final Logger log = LoggerFactory.getLogger(PatchHandler.class);

    private final String tusResumable;
    private final UploadManager uploadManager;

    @Inject
    public PatchHandler(@Value("${TusResumable}") String tusResumable, UploadManager uploadManager) {
        this.tusResumable = tusResumable;
        this.uploadManager = uploadManager;
    }

    void handleRequest(RoutingContext ctx, Vertx vertx) {
        log.info("In to PATCH Handler {}.", ctx);
        HttpServerResponse response = ctx.response();
        String uploadID = ctx.request().getParam("uploadID");
        Optional<String> contentType = getHeaderAsString("Content-Type", ctx);
        Optional<Long> offset = getHeaderAsLong("Upload-Offset", ctx);
        Optional<Long> contentLength = getHeaderAsLong("Content-Length", ctx);
        Optional<UploadInfo.ChecksumInfo> checksumInfo = getHeaderAsChecksumInfo("Upload-Checksum", ctx);
        Optional<UploadInfo> uploadInfo = uploadManager.findUploadInfo(uploadID);
        boolean rogueRequest = false;
        if (!contentType.isPresent() || !"application/offset+octet-stream".equals(contentType.get())) {
            rogueRequest = true;
            response.setStatusCode(400);
        }
        if (!uploadInfo.isPresent()) {
            rogueRequest = true;
            response.setStatusCode(400);
        }
        if (!offset.isPresent() || offset.get() < 0) {
            rogueRequest = true;
            response.setStatusCode(400);
        }
        if (!rogueRequest) {
            if (!uploadManager.acquireLock(uploadID)) {
                response.setStatusCode(423);
            } else {
                UploadInfo info = uploadInfo.get();
                if (offset.get() != info.getOffset() && checkContentLengthWithCurrentOffset(contentLength, offset.get(), info.getEntityLength())) {
                    response.setStatusCode(409);
                } else {
                    vertx.<Long>executeBlocking(future -> {
                        try {
                            long persisted = uploadManager.delegateToStoragePlugin(ctx.request(), uploadID, offset.get(), checksumInfo);
                            future.complete(persisted);
                        } finally {
                            uploadManager.releaseLock(uploadID);
                        }
                    }, res -> {
                        if (res.succeeded()) {
                            if (res.result() == -1) {
                                response.setStatusCode(404);
                            } else {
                                response.putHeader("Upload-Offset", String.valueOf(res.result()));
                                response.setStatusCode(204);
                            }
                        } else {
                            response.setStatusCode(500);
                        }
                        response.putHeader("Tus-Resumable", tusResumable);
                        response.end();
                    });
                }
            }
        }
    }

    private boolean checkContentLengthWithCurrentOffset(Optional<Long> contentLength, Long offset, Long entityLength) {
        return contentLength.map(aLong -> (aLong + offset <= entityLength)).orElse(true);
    }
}