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

package com.owlplug.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PlatformUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformUtils.class);

    public static void openFromDesktop(final String path) {
        if (path != null) {
            openFromDesktop(new File(path));
        } else {
            throw new IllegalArgumentException("path can't be null");
        }
    }

    public static void openFromDesktop(final File file) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            } else {
                LOGGER.warn("Desktop operations are not supported in this environment");
            }
        } catch (IOException e) {
            LOGGER.error("Application for the given file fails to be launched", e);
        }
    }

    public static void openDefaultBrowser(String url) {

        try {
            if (Desktop.isDesktopSupported()) {
                LOGGER.debug("Opening address {} in default browser", url);
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (final IOException e) {
            LOGGER.error("Can't open default browser", e);
        } catch (URISyntaxException e) {
            LOGGER.error("Error in URI: {}", url, e);
        }
    }

}
