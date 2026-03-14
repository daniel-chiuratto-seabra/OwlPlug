/* OwlPlug
 * Copyright (C) 2021-2024 Arthur <dropsnorz@gmail.com>
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

package com.owlplug;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.owlplug.util.PropertyUtils.getPreLoader;
import static java.lang.System.setProperty;
import static javafx.application.Application.launch;

/**
 * The Bootstrap class serves as the primary entry point for the OwlPlug application.
 * It is responsible for initializing the JavaFX environment and launching the main
 * OwlPlug application class. This separation is necessary because, since JDK 11,
 * a JavaFX Application class cannot contain the `main` method directly without
 * causing an error. This class ensures the correct setup of the JavaFX preloader
 * before the application fully starts.
 */
@SpringBootApplication
public class Bootstrap {

    private static final String JAVAFX_PRELOADER_PROPERTY = "javafx.preloader";

    /**
     * The main method that serves as the entry point for the OwlPlug application.
     * This method is responsible for setting up the JavaFX preloader property
     * and then launching the main JavaFX application class, {@link OwlPlug}.
     *
     * @param args Command line arguments passed to the application.
     */
    public static void main(final String... args) {
        // Manually load properties to set up the JavaFX preloader.
        // This must be done before the Spring context starts, as Spring injection
        // is not yet available at this early stage of application startup.
        setProperty(JAVAFX_PRELOADER_PROPERTY, getPreLoader());
        // Launch the main JavaFX application. Control is transferred to the JavaFX runtime,
        // which will then invoke the init() and start() methods of the OwlPlug class.
        launch(OwlPlug.class, args);
    }

}
