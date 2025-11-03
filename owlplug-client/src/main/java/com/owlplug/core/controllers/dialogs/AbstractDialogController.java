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

package com.owlplug.core.controllers.dialogs;

import com.owlplug.controls.Dialog;
import com.owlplug.controls.DialogLayout;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.components.ApplicationPreferences;
import com.owlplug.core.components.DialogManager;
import com.owlplug.core.controllers.BaseController;
import com.owlplug.core.services.TelemetryService;

public abstract class AbstractDialogController extends BaseController {

    private double width = -1;
    private double height = -1;

    private Dialog dialog;

    public AbstractDialogController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                                    final TelemetryService telemetryService, final DialogManager dialogManager) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
    }

    /**
     * Creates a new Dialog with a fixed size.
     *
     * @param width  dialog width
     * @param height dialog height
     */
    public AbstractDialogController(final ApplicationDefaults applicationDefaults, final ApplicationPreferences applicationPreferences,
                                    final TelemetryService telemetryService, final DialogManager dialogManager, final double width,
                                    final double height) {
        super(applicationDefaults, applicationPreferences, telemetryService, dialogManager);
        this.width = width;
        this.height = height;
    }

    protected abstract DialogLayout getLayout();

    /**
     * Open and display dialog frame.
     */
    public void show() {
        onDialogShow();
        if (width != -1 && height != -1) {
            dialog = getDialogManager().newDialog(width, height, getLayout());
        } else {
            dialog = getDialogManager().newDialog(getLayout());
        }

        dialog.setOnDialogClosed(e -> {
            onDialogClose();
            dialog = null;
        });

        dialog.setOnDialogOpened(e -> onDialogShow());

        dialog.setOverlayClose(true);
        dialog.show();
    }

    /**
     * Close dialog frame.
     */
    public void close() {
        if (dialog != null) {
            dialog.close();
        }
    }

    protected void setOverlayClose(final boolean overlayClose) {
        if (dialog != null) {
            dialog.setOverlayClose(overlayClose);
        }
    }

    protected void onDialogShow() {
    }

    protected void onDialogClose() {
    }

}
