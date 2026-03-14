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

package com.owlplug.explore.model.mappers.registry;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class BundleMapper {

    @Deprecated
    private String name;

    @Deprecated
    private String format;

    @Getter @Setter private List<String> targets;
    @Getter @Setter private String downloadUrl;
    @Getter @Setter private String downloadSha256;
    @Getter @Setter private List<String> formats;
    @Getter @Setter private String version;
    @Getter @Setter private String technicalUid;
    @Getter @Setter private long fileSize;

    @Deprecated
    public String getName() {
        return name;
    }

    @Deprecated
    public void setName(String name) {
        this.name = name;
    }

    @Deprecated
    public String getFormat() {
        return format;
    }

    @Deprecated
    public void setFormat(String format) {
        this.format = format;
    }

}
