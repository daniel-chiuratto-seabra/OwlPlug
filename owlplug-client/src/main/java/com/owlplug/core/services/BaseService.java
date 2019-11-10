/* OwlPlug
 * Copyright (C) 2019 Arthur <dropsnorz@gmail.com>
 *
 * This file is part of OwlPlug.
 *
 * OwlPlug is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OwlPlug is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OwlPlug.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.owlplug.core.services;

import com.owlplug.core.components.ApplicationDefaults;
import java.util.prefs.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseService {
  
  @Autowired
  private ApplicationDefaults applicationDefaults;
  @Autowired
  private Preferences preferences;
  
  
  public ApplicationDefaults getApplicationDefaults() {
    return applicationDefaults;
  }
  
  public Preferences getPreferences() {
    return preferences;
  }

}