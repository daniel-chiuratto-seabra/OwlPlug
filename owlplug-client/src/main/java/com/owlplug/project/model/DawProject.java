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

package com.owlplug.project.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Getter
@Setter
@Entity
@ToString
@EqualsAndHashCode
public class DawProject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String path;

    private String name;

    private DawApplication application;

    private String appFullName;

    private String formatVersion;

    private Date lastModifiedAt;

    private Date createdAt;

    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER, orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<DawPlugin> plugins = new HashSet<>();

    public List<DawPlugin> getPluginByLookupResult(final LookupResult result) {
        return plugins.stream().filter(lookUpResultFilter(result)).toList();
    }

    private static Predicate<DawPlugin> lookUpResultFilter(final LookupResult result) {
        return dawPlugin -> dawPlugin.getLookup() != null &&
                dawPlugin.getLookup().getResult().equals(result);
    }

}
