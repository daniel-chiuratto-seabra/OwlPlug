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

package com.owlplug.plugin.model;

import com.owlplug.project.model.DawPluginLookup;
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
import jakarta.persistence.Inheritance;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance
@Table(indexes = {@Index(name = "IDX_PLUGIN_ID", columnList = "id"),
        @Index(name = "IDX_PLUGIN_NAME", columnList = "name")})
public class Plugin {

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Getter
    @Setter
    protected String name;

    @Getter
    @Setter
    protected String descriptiveName;

    @Getter
    @Setter
    protected String uid;

    @Getter
    @Setter
    protected String category;

    @Getter
    @Setter
    protected String manufacturerName;

    @Getter
    @Setter
    protected String identifier;

    @Getter
    @Setter
    protected String path;

    @Getter
    @Setter
    protected String scanDirectoryPath;

    @Getter
    @Setter
    protected String bundleId;

    @Getter
    @Setter
    protected String version;

    // Suggestion: could be renamed to screenshotURI
    @Getter
    @Setter
    protected String screenshotUrl;

    @Getter
    @Setter
    protected boolean nativeCompatible = false;

    @Getter
    @Setter
    protected boolean scanComplete = false;

    @Getter
    @Setter
    @Column(columnDefinition = "boolean default false")
    protected boolean disabled = false;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    protected PluginFormat format;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    protected PluginType type;

    @Getter
    @Setter
    @OneToOne
    protected PluginFootprint footprint;

    @Getter
    @Setter
    @OneToMany(mappedBy = "plugin", orphanRemoval = true, fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private Set<PluginComponent> components = new HashSet<>();

    @OneToMany(mappedBy = "plugin")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<DawPluginLookup> lookups;

    @Override
    public String toString() {
        return name;
    }

}
