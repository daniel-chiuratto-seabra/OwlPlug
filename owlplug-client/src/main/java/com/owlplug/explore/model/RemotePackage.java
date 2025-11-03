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

import com.owlplug.plugin.model.PluginStage;
import com.owlplug.plugin.model.PluginType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(indexes = {@Index(name = "IDX_PACKAGE_ID", columnList = "id"), @Index(name = "IDX_PACKAGE_NAME", columnList = "name")})
public class RemotePackage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String slug;

    private String pageUrl;

    @Deprecated
    private String downloadUrl;

    private String screenshotUrl;

    private String donateUrl;

    private String creator;

    private String license;

    private String version;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    private PluginType type;

    @Enumerated(EnumType.STRING)
    private PluginStage stage;

    @ManyToOne
    private RemoteSource remoteSource;

    @OneToMany(mappedBy = "remotePackage", orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<PackageBundle> bundles = new HashSet<>();

    @OneToMany(mappedBy = "remotePackage", orphanRemoval = true, fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<PackageTag> tags = new HashSet<>();

    @Deprecated
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Deprecated
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

}
