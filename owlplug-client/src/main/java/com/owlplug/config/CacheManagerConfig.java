package com.owlplug.config;

import org.ehcache.CacheManager;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.owlplug.core.components.ApplicationDefaults.getUserDataDirectory;
import static java.nio.file.Paths.get;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.config.builders.CacheManagerBuilder.persistence;
import static org.ehcache.config.builders.ResourcePoolsBuilder.newResourcePoolsBuilder;

/**
 * Spring configuration for the EhCache {@link CacheManager}.
 * <p>
 * This class is responsible for setting up the application's caching mechanism.
 * It defines a persistent, disk-backed cache named "image-cache" for storing
 * image data, which helps improve performance by reducing redundant data fetching.
 */
@Configuration
public class CacheManagerConfig {
    /**
     * Initializes and configures the EhCache {@link CacheManager} instance for the application.
     * This method sets up a persistent, disk-backed cache named "image-cache"
     * designed to store image data efficiently. The cache uses a heap of 100 MB
     * and a disk store of 700 MB, ensuring that frequently accessed images are
     * quickly available and larger sets of images can be persisted across sessions.
     *
     * @return A fully initialized and configured {@link CacheManager} instance.
     */
    @Bean
    public CacheManager getCacheManager() {
        // Build a new CacheManager instance.
        final var cacheManager = newCacheManagerBuilder()
                // Configure persistence for the cache manager, storing cache data in a "cache" directory
                // within the user's application data directory.
                .with(persistence(get(getUserDataDirectory(), "cache").toString()))
                // Define and configure a specific cache named "image-cache".
                .withCache("image-cache", newCacheConfigurationBuilder(
                        String.class, // Key type for the cache (e.g., image URLs or identifiers).
                        byte[].class, // Value type for the cache (e.g., image byte arrays).
                        // Configure resource pools for the "image-cache".
                        newResourcePoolsBuilder()
                                .heap(100, MemoryUnit.MB) // 100 MB in-memory heap for fast access.
                                .disk(700, MemoryUnit.MB, true) // 700 MB disk store, persistent across restarts.
                ))
                .build(); // Finalize the CacheManager configuration.
        // Initialize the configured CacheManager. This makes the caches available for use.
        cacheManager.init();
        return cacheManager; // Return the initialized CacheManager.
    }
}
