package com.owlplug.host.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ClassPathVersionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathVersionUtils.class);

    public static String getVersion(final String resource) throws IOException {
        final var filename = "%s.version".formatted(resource);
        try (final var inputStream = ClassPathVersionUtils.class.getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new IOException("Resource %s not found".formatted(filename));
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static String getVersionSafe(final String resource) {
        try {
            return getVersion(resource);
        } catch (final IOException ioException) {
            LOGGER.error("Version can't be retrieved from resource {}", resource, ioException);
        }
        return "undefined-version";
    }
}
