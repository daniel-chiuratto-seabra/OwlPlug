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

package com.owlplug.explore.model;

import com.owlplug.plugin.model.PluginFormat;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PackageBundle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Deprecated
    private String name;

    private String downloadUrl;

    private String downloadSha256;

    @Deprecated
    @Enumerated(EnumType.STRING)
    private PluginFormat format;

    private String technicalUid;

    private String version;

    private long fileSize;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> targets;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> formats;

    @ManyToOne
    private RemotePackage remotePackage;

    @Deprecated
    public String getName() {
        return name;
    }

    @Deprecated
    public void setName(String name) {
        this.name = name;
    }

    @Deprecated
    public PluginFormat getFormat() {
        return format;
    }

    @Deprecated
    public void setFormat(PluginFormat format) {
        this.format = format;
    }

}
