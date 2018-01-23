package com.tus.oss.server.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * @author ckaratza
 * A helper class for creating the spring application context.
 */
final class SpringContextUtils {

    private static final Logger logger = LoggerFactory.getLogger(SpringContextUtils.class);


    static ApplicationContext bootSpringApplication(SpringBootConfig bootConfig) {
        ConfigurableEnvironment env = new StandardEnvironment();
        MutablePropertySources propertySources = env.getPropertySources();
        bootConfig.getPropsFiles().stream()
                .map(f -> {
                    try {
                        return new PropertiesPropertySource(f.toString(), PropertiesLoaderUtils.loadProperties(new FileSystemResource(f)));
                    } catch (IOException e) {
                        logger.error("Failed to load {}:{}.", f, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(propertySources::addFirst);
        SpringApplication app = new SpringApplication(bootConfig.getSpringConfigFiles().toArray(new Object[bootConfig.getSpringConfigFiles().size()]));
        app.setLogStartupInfo(true);
        app.setMainApplicationClass(bootConfig.getClass());
        app.setEnvironment(env);
        app.setRegisterShutdownHook(true);
        ConfigurableApplicationContext applicationContext = app.run(bootConfig.getAppArgs());
        applicationContext.registerShutdownHook();
        return applicationContext;
    }
}
