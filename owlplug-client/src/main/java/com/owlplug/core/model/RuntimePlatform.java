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

package com.owlplug.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RuntimePlatform {

    @Getter
    @Setter
    private String tag;

    @Getter
    @Setter
    private OperatingSystem operatingSystem;

    @Getter
    @Setter
    private String arch;

    @Getter
    @Setter
    private Collection<RuntimePlatform> compatiblePlatforms = new ArrayList<>();

    private final List<String> aliases = new ArrayList<>();

    public RuntimePlatform(final String tag, final OperatingSystem operatingSystem, final String arch) {
        super();
        this.tag = tag;
        this.operatingSystem = operatingSystem;
        this.arch = arch;

        this.compatiblePlatforms.add(this);
    }

    public RuntimePlatform(final String tag, final OperatingSystem operatingSystem, final String arch, final String[] aliases) {
        this(tag, operatingSystem, arch);
        this.aliases.addAll(Arrays.stream(aliases).toList());
    }

    public Set<String> getCompatiblePlatformsTags() {
        final var platforms = new HashSet<String>();
        platforms.add(this.operatingSystem.getCode());
        platforms.addAll(aliases);
        for (final var runtimePlatform : compatiblePlatforms) {
            platforms.add(runtimePlatform.getTag());
        }
        return platforms;
    }

    @Override
    public String toString() {
        return "RuntimePlatform [tag=" + tag + ", operatingSystem=" + operatingSystem + ", arch=" + arch + "]";
    }

}
