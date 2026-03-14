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

package com.owlplug.project.tasks;

import com.owlplug.core.tasks.AbstractTask;
import com.owlplug.core.tasks.TaskResult;
import com.owlplug.core.utils.FileUtils;
import com.owlplug.project.model.DawProject;
import com.owlplug.project.repositories.DawProjectRepository;
import com.owlplug.project.tasks.discovery.ableton.AbletonProjectExplorer;
import com.owlplug.project.tasks.discovery.reaper.ReaperProjectExplorer;
import com.owlplug.project.tasks.discovery.studioone.StudioOneProjectExplorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectSyncTask extends AbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectSyncTask.class);

    private final DawProjectRepository projectRepository;
    private final List<String> projectDirectories;

    public ProjectSyncTask(final DawProjectRepository projectRepository, final List<String> projectDirectories) {
        this.projectRepository = projectRepository;
        this.projectDirectories = projectDirectories;
        setName("Sync DAW projects");
    }

    @Override
    protected TaskResult start() throws Exception {
        LOGGER.debug("Starting project sync task");
        updateProgress(0, 1);

        projectRepository.deleteAll();

        // Collect files from all project directories
        final var baseFiles = new ArrayList<File>();
        for (final var directory : projectDirectories) {
            final var dir = new File(directory);
            updateMessage("Syncing projects from: %s".formatted(dir.getAbsolutePath()));
            if (dir.isDirectory()) {
                baseFiles.addAll(FileUtils.listUniqueFilesAndDirs(dir));
            }
        }

        // Filter collected files
        final var filteredFiles = baseFiles.stream()
                // Filter out HFS metadata files starting with "._"
                .filter(file -> !file.getName().startsWith("._"))
                .toList();

        setMaxProgress(filteredFiles.size());

        // Create explorer instances once, outside the loop (they are stateless)
        final var abletonExplorer = new AbletonProjectExplorer();
        final var reaperExplorer = new ReaperProjectExplorer();
        final var studioOneExplorer = new StudioOneProjectExplorer();

        for (final var file : filteredFiles) {
            commitProgress(1);

            // Explores and persists a project from a matching file explorer
            if (abletonExplorer.canExploreFile(file)) {
                updateMessage("Analyzing Ableton file: %s".formatted(file.getAbsolutePath()));
                final var project = abletonExplorer.explore(file);
                if (project != null) {
                    projectRepository.save(project);
                }
            } else if (reaperExplorer.canExploreFile(file)) {
                updateMessage("Analyzing Reaper file: %s".formatted(file.getAbsolutePath()));
                final var project = reaperExplorer.explore(file);
                if (project != null) {
                    projectRepository.save(project);
                }
            } else if (studioOneExplorer.canExploreFile(file)) {
                updateMessage("Analyzing Studio One file: %s".formatted(file.getAbsolutePath()));
                final var project = studioOneExplorer.explore(file);
                if (project != null) {
                    projectRepository.save(project);
                }
            }
        }

        updateMessage("All projects are synchronized");
        updateProgress(1, 1);

        return completed();
    }
}
