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

package com.owlplug.plugin.tasks.discovery;

import com.owlplug.core.model.RuntimePlatform;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PluginScanTaskParameters {

    private RuntimePlatform platform;
    private String directoryScope;
    private String vst2Directory;
    private String vst3Directory;
    private String auDirectory;
    private String lv2Directory;
    private boolean findVst2;
    private boolean findVst3;
    private boolean findAu;
    private boolean findLv2;
    private List<String> vst2ExtraDirectories;
    private List<String> vst3ExtraDirectories;
    private List<String> auExtraDirectories;
    private List<String> lv2ExtraDirectories;
    private boolean differential;

}
