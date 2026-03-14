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

package com.owlplug.explore.model.search;

import com.owlplug.explore.model.RemotePackage;
import com.owlplug.explore.repositories.RemotePackageRepository;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static com.owlplug.explore.repositories.RemotePackageRepository.hasCreator;
import static com.owlplug.explore.repositories.RemotePackageRepository.hasFormat;
import static com.owlplug.explore.repositories.RemotePackageRepository.hasPlatformTag;
import static com.owlplug.explore.repositories.RemotePackageRepository.hasTag;
import static com.owlplug.explore.repositories.RemotePackageRepository.isTyped;
import static com.owlplug.explore.repositories.RemotePackageRepository.nameContains;
import static java.lang.String.valueOf;

public class ExploreCriteriaAdapter {

    public static Specification<RemotePackage> toSpecification(final List<ExploreFilterCriteria> criteriaList) {
        Specification<RemotePackage> specification = Specification.unrestricted();
        for (ExploreFilterCriteria criteria : criteriaList) {
            specification = specification.and(toSpecification(criteria));
        }
        return specification;
    }

    public static Specification<RemotePackage> toSpecification(final ExploreFilterCriteria exploreFilterCriteria) {
        return switch (exploreFilterCriteria.getFilterType()) {
            case NAME -> nameContains(valueOf(exploreFilterCriteria.getValue()));
            case TAG -> hasTag(valueOf(exploreFilterCriteria.getValue()));
            case TYPE -> isTyped(exploreFilterCriteria.getValue());
            case PLATFORM -> hasPlatformTag(valueOf(exploreFilterCriteria.getValue()));
            case PLATFORM_LIST -> {
                final List<String> platformTagList = exploreFilterCriteria.getValue();
                yield hasPlatformTag(platformTagList);
            }
            case CREATOR -> hasCreator(valueOf(exploreFilterCriteria.getValue()));
            case FORMAT -> hasFormat(valueOf(exploreFilterCriteria.getValue()));
            case FORMAT_LIST -> {
                final List<String> formatList = exploreFilterCriteria.getValue();
                yield hasFormat(formatList);
            }
        };
    }
}
