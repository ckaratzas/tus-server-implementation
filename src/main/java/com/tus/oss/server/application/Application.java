package com.tus.oss.server.application;

import com.tus.oss.server.core.ServerVerticle;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * @author ckaratza
 * The entry point of the Tus Server Implementation. Loads all configuration, creates application context and starts Tus Server Verticle.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringBootConfig bootConfig = SpringBootConfig.fromCommandLineArgs(args, Optional.empty());
        ApplicationContext ctx = SpringContextUtils.bootSpringApplication(bootConfig);
        ServerVerticle serverVerticle = ctx.getBean(ServerVerticle.class);
        Vertx.vertx().deployVerticle(serverVerticle);
        serverVerticle.start();
        log.info("Tus Server started...");
    }
}
