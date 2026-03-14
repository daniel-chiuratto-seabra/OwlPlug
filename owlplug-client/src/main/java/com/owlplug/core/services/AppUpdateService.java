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

package com.owlplug.core.services;

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.vdurmont.semver4j.Semver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@Service
public class AppUpdateService extends BaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUpdateService.class);

    public AppUpdateService(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences) {
        super(applicationDefaults, applicationPreferences);
    }

    /**
     * Returns application update status based on OwlPlug Hub remote version.
     *
     * @return true if the app is up to date, false otherwise
     */
    public boolean isUpToDate() {
        String remoteVersion = getLastVersion();

        if (remoteVersion != null) {
            Semver remoteSemver = new Semver(remoteVersion);
            Semver currentSemver = new Semver(this.getApplicationDefaults().getVersion());
            return remoteSemver.isLowerThanOrEqualTo(currentSemver);

        }
        return true;
    }

    private String getLastVersion() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(((request, body, execution) -> {
            request.getHeaders().add("User-Agent", "OwlPlug/App");
            request.getHeaders().add("Accept", "application/vnd.github+json");
            return execution.execute(request, body);
        }));

        final var url = getApplicationDefaults().getLatestUrl();

        try {
            // Map GitHub JSON to a simple Map, so we can extract "tag_name"
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (requireNonNull(response).containsKey("tag_name")) {
                return (String) response.get("tag_name");
            }
        } catch (final RestClientException e) {
            LOGGER.error("Error retrieving latest GitHub release version", e);
        }
        return null;
    }
}
