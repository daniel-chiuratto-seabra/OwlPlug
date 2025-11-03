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

package com.owlplug.project.tasks.discovery.reaper;

import com.owlplug.core.utils.FileUtils;
import com.owlplug.parsers.reaper.PluginNodeListener;
import com.owlplug.parsers.reaper.ReaperProjectLexer;
import com.owlplug.parsers.reaper.ReaperProjectParser;
import com.owlplug.plugin.model.PluginFormat;
import com.owlplug.project.model.DawApplication;
import com.owlplug.project.model.DawPlugin;
import com.owlplug.project.model.DawProject;
import com.owlplug.project.tasks.discovery.ProjectExplorer;
import com.owlplug.project.tasks.discovery.ProjectExplorerException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

public class ReaperProjectExplorer implements ProjectExplorer {

    @Override
    public boolean canExploreFile(final File file) {
        return file.isFile() && file.getAbsolutePath().endsWith(".rpp");
    }


    @Override
    public DawProject explore(final File file) throws ProjectExplorerException {

        final var dawProject = new DawProject();
        dawProject.setApplication(DawApplication.REAPER);
        dawProject.setAppFullName("Reaper");
        dawProject.setPath(FileUtils.convertPath(file.getAbsolutePath()));
        dawProject.setName(FilenameUtils.removeExtension(file.getName()));

        try {
            dawProject.setLastModifiedAt(new Date(file.lastModified()));
            final var basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

            final var fileTime = basicFileAttributes.creationTime();
            dawProject.setCreatedAt(Date.from(fileTime.toInstant()));

            final var fileInputStream = new FileInputStream(file);

            final var reaperProjectLexer = new ReaperProjectLexer(CharStreams.fromStream(fileInputStream));
            final var commonTokenStream = new CommonTokenStream(reaperProjectLexer);
            final var reaperProjectParser = new ReaperProjectParser(commonTokenStream);
            final var nodeContext = reaperProjectParser.node();

            final var parseTreeWalker = new ParseTreeWalker();
            final var pluginNodeListener = new PluginNodeListener();
            parseTreeWalker.walk(pluginNodeListener, nodeContext);

            pluginNodeListener.getReaperPlugins().forEach(reaperPlugin -> {
                final var dawPlugin = new DawPlugin();
                dawPlugin.setProject(dawProject);
                dawPlugin.setName(FilenameUtils.removeExtension(reaperPlugin.getFilename()));
                dawPlugin.setPath(reaperPlugin.getFilename());

                if (reaperPlugin.getName().contains("VST3")) {
                    dawPlugin.setFormat(PluginFormat.VST3);
                } else {
                    dawPlugin.setFormat(PluginFormat.VST2);
                }
                dawProject.getPlugins().add(dawPlugin);
            });

            return dawProject;

        } catch (final IOException ioException) {
            throw new ProjectExplorerException("Error while opening Reaper project %s".formatted(file.getAbsolutePath()), ioException);
        }
    }
}
