package com.owlplug.project.controllers.common;

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.plugin.model.PluginFormat;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;

public class TableColumnUtils {
    public static <T> TableCell<T, PluginFormat> cellFactory(final TableColumn<T, PluginFormat> tableColumn,
                                                              final ApplicationDefaults applicationDefaults) {
        return new TableCell<>() {
            @Override
            public void updateItem(final PluginFormat pluginFormat, final boolean empty) {
                super.updateItem(pluginFormat, empty);
                if (pluginFormat == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(pluginFormat.getText());
                    setGraphic(new ImageView(applicationDefaults.getPluginFormatIcon(pluginFormat)));
                }
            }
        };
    }
}
