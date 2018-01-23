package com.tus.oss.server.application;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author ckaratza
 * This class hold configuration required to boot a new Spring application.
 */
class SpringBootConfig {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootConfig.class);

    private static final CommandLineParser defaultCommandLineParser = new PosixParser();
    private static final Options defaultCommandLineOptions;
    private final static String DEFAULT_CONFIG_DIR = "config";
    private final static String DEFAULT_PROPS_FILE = "application.properties";
    private final static String DEFAULT_CUSTOM_BEANS = "custom.beans.xml";

    static {
        defaultCommandLineOptions = new Options();
        defaultCommandLineOptions.addOption("c", "configuration", true, "Configuration directory");
        defaultCommandLineOptions.addOption("p", "properties", true, "Properties file");
        defaultCommandLineOptions.addOption("b", "spring-configuration", true, "Custom Spring configuration file");
    }

    private final String[] appArgs;
    private final File configDir;
    private final List<File> propsFiles;
    private final List<String> springConfigFiles;

    /**
     * @param appArgs           pass through application arguments
     * @param configDir         the directory that contains the configuration files for the application
     * @param propsFiles        the properties files
     * @param springConfigFiles the spring configuration files
     */
    private SpringBootConfig(String[] appArgs,
                             String configDir,
                             List<String> propsFiles,
                             List<String> springConfigFiles) {

        this.appArgs = appArgs;
        this.springConfigFiles = Collections.unmodifiableList(springConfigFiles);
        this.configDir = new File(configDir.trim());

        if (!this.configDir.exists() || !this.configDir.isDirectory()) {
            throw new IllegalArgumentException(String.format("Bad config directory: %s.", this.configDir.getAbsolutePath()));
        }

        this.propsFiles = propsFiles.stream().map(
                p -> new File(this.configDir, p.trim())).peek(f -> {
                    if (!f.exists() || !f.canRead()) {
                        throw new IllegalArgumentException(String.format("Bad properties file: %s.", f.getAbsolutePath()));
                    }
                }
        ).collect(Collectors.toList());

        logger.info("Config directory:    {}.", this.configDir);
        logger.info("Properties files:    {}.", propsFiles);
        logger.info("Spring config files: {}.", springConfigFiles);
    }

    static SpringBootConfig fromCommandLineArgs(String[] args, Optional<String> defaultBeans) {
        logger.debug("fromCommandLineArgs: args = {}. Default beans {}.", Arrays.asList(args), defaultBeans);
        /* Parse command line arguments */
        CommandLine cmd;
        try {
            cmd = defaultCommandLineParser.parse(defaultCommandLineOptions, args, true);
        } catch (ParseException e) {
            logger.error("Error while parsing application arguments: {}.", Arrays.asList(args));
            throw new IllegalArgumentException(e);
        }

        String configDir = cmd.getOptionValue("c", DEFAULT_CONFIG_DIR);
        String customBeans = cmd.getOptionValue("b", DEFAULT_CUSTOM_BEANS);

        File customBeansFile = new File(configDir, customBeans);

        List<String> springConfigFiles = customBeansFile.exists()
                ? Collections.singletonList("file://" + customBeansFile.getAbsolutePath()) : Collections.emptyList();
        defaultBeans.ifPresent(springConfigFiles::add);
        Assert.isTrue(!springConfigFiles.isEmpty(), "No spring beans provided!");

        List<String> propertiesFiles = cmd.hasOption("p")
                ? Arrays.asList(cmd.getOptionValues("p"))
                : Collections.singletonList(DEFAULT_PROPS_FILE);

        return new SpringBootConfig(cmd.getArgs(), configDir, propertiesFiles, springConfigFiles);
    }

    List<File> getPropsFiles() {
        return this.propsFiles;
    }

    List<String> getSpringConfigFiles() {
        return this.springConfigFiles;
    }

    String[] getAppArgs() {
        return this.appArgs;
    }

}
