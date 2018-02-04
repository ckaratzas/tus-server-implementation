package com.tus.oss.server.core;

import com.tus.oss.server.openapi.OpenApiRoutePublisher;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author ckaratza
 * The Tus Server Verticle with the route definitions.
 */
@Component
public class ServerVerticle extends AbstractVerticle {

    private final Integer port;
    private final String host;
    private final String contextPath;
    private final OptionsHandler optionsHandler;
    private final HeadHandler headHandler;
    private final PostHandler postHandler;
    private final PatchHandler patchHandler;
    private final DeleteHandler deleteHandler;

    @Inject
    public ServerVerticle(@Value("${port}") Integer port, @Value("${host}") String host, @Value("${contextPath}") String contextPath,
                          OptionsHandler optionsHandler, HeadHandler headHandler, PostHandler postHandler,
                          PatchHandler patchHandler, DeleteHandler deleteHandler) {
        this.port = port;
        this.host = host;
        this.contextPath = contextPath;
        this.optionsHandler = optionsHandler;
        this.headHandler = headHandler;
        this.postHandler = postHandler;
        this.patchHandler = patchHandler;
        this.deleteHandler = deleteHandler;
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create()).handler(LoggerHandler.create()).handler(ResponseTimeHandler.create()).enable();
        router.head(contextPath + ":uploadID").handler(headHandler::handleRequest);
        router.options(contextPath).handler(optionsHandler::handleRequest);
        router.post(contextPath).handler(postHandler::handleRequest);
        router.delete(contextPath + ":uploadID").handler(deleteHandler::handleRequest);
        router.patch(contextPath + ":uploadID").handler(patchHandler::handleRequestForPatch);
        //POST can replace PATCH because of buggy jre...
        router.post(contextPath + ":uploadID").handler(patchHandler::handleRequestForPost);
        OpenApiRoutePublisher.publishOpenApiSpec(router, contextPath + "spec",
                "Tus.io Resumable File Upload Protocol Server", "1.0.0", "http://" + host + ":" + port + "/");
        vertx.createHttpServer().requestHandler(router::accept).listen(port, host);
    }
}