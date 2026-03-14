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

package com.owlplug;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.File;
import java.util.prefs.Preferences;

import static com.owlplug.core.components.ApplicationDefaults.VST2_DISCOVERY_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST3_DISCOVERY_ENABLED_KEY;
import static com.owlplug.core.components.ApplicationDefaults.VST_DIRECTORY_KEY;
import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppTestContext {

    @MockitoBean
    protected Preferences preferences;

    @BeforeAll
    public void setUp() {
        final var classLoader = getClass().getClassLoader();
        final var file = new File(requireNonNull(classLoader.getResource("test-data")).getFile());
        final var vstDirectoryTestPath = file.getAbsolutePath();

        when(preferences.getBoolean(any(String.class), any(Boolean.class)))
                .thenAnswer((Answer<?>) invocation -> {
                    final var args = invocation.getArguments();
                    return switch ((String) args[0]) {
                        case VST2_DISCOVERY_ENABLED_KEY, VST3_DISCOVERY_ENABLED_KEY -> true;
                        default -> false;
                    };
                });

        when(preferences.get(any(String.class), any(String.class)))
                .thenAnswer((Answer<?>) invocation -> {
                    final var args = invocation.getArguments();
                    if ((args[0]).equals(VST_DIRECTORY_KEY)) {
                        return vstDirectoryTestPath;
                    }
                    return null;
                });
    }

}
