/* OwlPlug
 * Copyright (C) 2021 Arthur <dropsnorz@gmail.com>
 *
 * This file is part of OwlPlug.
 *
 * OwlPlug is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OwlPlug is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OwlPlug.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.owlplug.core.components;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.vdurmont.semver4j.Semver;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

/**
 * Performs data cleanup on the workspace directory User data migration is not
 * supported for now. Database schema is updated using the "hibernate" auto-ddl = update feature,
 * which is limited in case of major relational changes.
 * <p>
 * When the schema can't be updated by "hibernate" the database will be flushed.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class WorkspaceDirectoryInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceDirectoryInitializer.class);

    private final ApplicationDefaults applicationDefaults;

    @PostConstruct
    private void postConstruct() {
        cleanup();
        setupCustomLogLevel();
    }

    public void cleanup() {
        File workingDirectory = new File(ApplicationDefaults.getUserDataDirectory());

        if (!workingDirectory.exists()) {
            if (!workingDirectory.mkdirs()) {
                LOGGER.warn("For some reason the working directory could not been created");
            }
        }

        File versionFile = new File(workingDirectory, ".version");
        if (versionFile.exists()) {
            try {
                String workspaceVersion = FileUtils.readFileToString(versionFile, UTF_8);
                Semver workspaceSemver = new Semver(workspaceVersion);
                Semver workspaceMinSemver = new Semver(applicationDefaults.getWorkspaceMinVersion());

                if (workspaceSemver.isLowerThan(workspaceMinSemver)) {
                    LOGGER.info("Cleaning outdated workspace data from version {} to match constraint {}", workspaceVersion, workspaceMinSemver);
                    File dbFile = new File(workingDirectory, "owlplug.mv.db");
                    if (!dbFile.delete()) {
                        LOGGER.warn("For some reason the {} file can't be deleted", dbFile.getPath());
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Workspace version can't be retrieved from file", e);
            }

            if (!versionFile.delete()) {
                LOGGER.warn("For some reason the version file {} could not be deleted", versionFile.getPath());
            }
        }

        try {
            String currentVersion = applicationDefaults.getVersion();
            if (!versionFile.createNewFile()) {
                LOGGER.warn("For some reason the version file {} could not be created", versionFile.getPath());
            }
            FileUtils.writeStringToFile(versionFile, currentVersion, UTF_8);
        } catch (final IOException e) {
            LOGGER.error("Version file can't be created in workspace directory", e);
        }
    }

    /**
     * Retrieve user-defined log level in a logging.properties file on the workspace
     * directory.
     */
    public void setupCustomLogLevel() {

        File workingDirectory = new File(ApplicationDefaults.getUserDataDirectory());
        File loggingFile = new File(workingDirectory, "logging.properties");

        if (loggingFile.exists()) {
            LOGGER.info("Found custom logging properties {}", loggingFile.getPath());
            final var allowedLogLevels = asList("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF", "ALL");
            Properties loggingProperties = new Properties();
            try {
                loggingProperties.load(new FileInputStream(loggingFile));

                LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                List<ch.qos.logback.classic.Logger> loggerList = loggerContext.getLoggerList();
                loggerList.forEach(logger -> {
                    if (loggingProperties.containsKey(logger.getName())) {
                        final var logLevelStr = loggingProperties.getProperty(logger.getName());
                        if (allowedLogLevels.parallelStream().anyMatch(logLevelStr::contains)) {
                            Level level = Level.toLevel(logLevelStr.toUpperCase());
                            logger.setLevel(level);
                            LOGGER.info("Log level for {} set to {}", logger.getName(), logLevelStr);
                        } else {
                            LOGGER.error("Unknown log level {} for logger {}", logLevelStr, logger.getName());
                        }
                    }
                });
            } catch (IOException e) {
                LOGGER.error("Error while parsing custom log file {}", loggingFile.getPath(), e);
            }
        }
    }
}
