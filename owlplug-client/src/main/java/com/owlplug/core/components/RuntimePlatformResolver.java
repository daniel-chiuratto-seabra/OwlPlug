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

import com.owlplug.core.model.OperatingSystem;
import com.owlplug.core.model.RuntimePlatform;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.owlplug.core.model.OperatingSystem.LINUX;
import static com.owlplug.core.model.OperatingSystem.MAC;
import static com.owlplug.core.model.OperatingSystem.WIN;

@Component
public class RuntimePlatformResolver {

    private final Set<RuntimePlatform> platforms = new HashSet<>();

    @Getter
    private final RuntimePlatform currentPlatform;

    public RuntimePlatformResolver() {
        final var winX32 = new RuntimePlatform("win-x32", OperatingSystem.WIN, "x32", new String[]{"win32"});
        platforms.add(winX32);

        final var winX64 = new RuntimePlatform("win-x64", WIN, "x64", new String[]{"win64"});
        platforms.add(winX64);
        winX64.getCompatiblePlatforms().add(winX32);

        final var winArm64 = new RuntimePlatform("win-arm64", OperatingSystem.WIN, "arm64");
        platforms.add(winArm64);

        final var winArm64ec = new RuntimePlatform("win-arm64ec", OperatingSystem.WIN, "arm64ec");
        platforms.add(winArm64ec);
        winArm64ec.getCompatiblePlatforms().add(winX64);

        final var macX64 = new RuntimePlatform("mac-x64", MAC, "x64");
        platforms.add(macX64);

        final var macArm64 = new RuntimePlatform("mac-arm64", MAC, "arm64");
        platforms.add(macArm64);

        final var linuxX86 = new RuntimePlatform("linux-x32", LINUX, "x32", new String[]{"linux32"});
        platforms.add(linuxX86);

        final var linuxX64 = new RuntimePlatform("linux-x64", LINUX, "x64", new String[]{"linux64"});
        platforms.add(linuxX64);
        linuxX64.getCompatiblePlatforms().add(linuxX86);

        final var linuxArm86 = new RuntimePlatform("linux-arm32", LINUX, "arm32");
        platforms.add(linuxArm86);

        final var linuxArm64 = new RuntimePlatform("linux-arm64", LINUX, "arm64");
        platforms.add(linuxArm64);
        linuxArm64.getCompatiblePlatforms().add(linuxArm86);

        currentPlatform = resolve();

        LoggerFactory.getLogger(RuntimePlatformResolver.class)
                     .info("Runtime Platform Resolved: {}", currentPlatform.toString());
    }

    public RuntimePlatform resolve() {
        final var os = resolveOperatingSystem();
        final var arch = resolveArchitecture();

        Optional<RuntimePlatform> platform = platforms.stream()
                .filter(p -> p.getOperatingSystem().equals(os))
                .filter(p -> p.getArch().equals(arch))
                .findFirst();

        return platform.orElseGet(() -> new RuntimePlatform("unknown", os, arch));
    }

    private String resolveArchitecture() {
        String arch = System.getProperty("os.arch");
        if (arch == null) {
            return "Unknown";
        }

        arch = arch.toLowerCase();

        if (arch.matches("^(amd64|x86_64)$")) {
            return "x64";
        } else if (arch.matches("^(i[3-6]86|x86)$")) {
            return "x32";
        } else if (arch.matches("^(aarch64|arm64)$")) {
            return "arm64";
        } else if (arch.matches("^(arm|arm32)$")) {
            return "arm32";
        } else if (arch.matches("^(arm64ec)$")) {
            return "arm64ec";
        } else {
            return "unknown";
        }
    }

    private OperatingSystem resolveOperatingSystem() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return WIN;
        }
        if (osName.contains("mac")) {
            return MAC;
        }
        if (osName.contains("nix") || osName.contains("nux")) {
            return LINUX;
        }
        return OperatingSystem.UNKNOWN;
    }

}
