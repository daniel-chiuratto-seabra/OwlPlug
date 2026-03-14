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

package com.owlplug.explore.tasks;

import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.core.model.RuntimePlatform;
import com.owlplug.core.tasks.AbstractTask;
import com.owlplug.core.tasks.TaskException;
import com.owlplug.core.tasks.TaskResult;
import com.owlplug.core.utils.ArchiveUtils;
import com.owlplug.core.utils.CryptoUtils;
import com.owlplug.core.utils.FileUtils;
import com.owlplug.core.utils.nio.CallbackByteChannel;
import com.owlplug.explore.model.PackageBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.Objects.requireNonNull;

public class BundleInstallTask extends AbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(BundleInstallTask.class);

    private final PackageBundle bundle;
    private final File targetDirectory;
    private final ApplicationDefaults applicationDefaults;

    /**
     * Creates a new Package Bundle installation task.
     *
     * @param packageBundle       Bundle to download
     * @param targetDirectory     Target directory where the downloaded package is
     *                            stored
     * @param applicationDefaults Owlplug ApplicationDefaults
     */
    public BundleInstallTask(final PackageBundle packageBundle, final File targetDirectory, final ApplicationDefaults applicationDefaults) {
        bundle = packageBundle;
        this.targetDirectory = targetDirectory;
        this.applicationDefaults = applicationDefaults;
        setName("Install plugin - %s".formatted(packageBundle.getRemotePackage().getName()));
        setMaxProgress(150);
    }

    @Override
    protected TaskResult start() throws Exception {

        try {
            boolean created = targetDirectory.mkdirs();
            if (!targetDirectory.exists() && !created) {
                updateMessage("Installing plugin " + bundle.getRemotePackage().getName() + " - Can't create installation directory");
                LOGGER.error("Can't create installation directory. ");
                throw new TaskException("Can't create installation directory");
            } else if (!targetDirectory.isDirectory()) {
                updateMessage("Installing plugin " + bundle.getRemotePackage().getName() + " - Invalid installation directory");
                LOGGER.error("Invalid plugin installation target directory");
                throw new TaskException("Invalid plugin installation target directory");
            }
            updateMessage("Installing plugin " + bundle.getRemotePackage().getName() + " - Downloading files...");
            File archiveFile = downloadInTempDirectory(bundle);

            updateMessage("Installing plugin " + bundle.getRemotePackage().getName() + " - Verifying files...");

            if (bundle.getDownloadSha256() != null && !bundle.getDownloadSha256().isBlank()) {
                LOGGER.debug("Verify downloaded file hash for bundle {}", bundle.getName());
                if (!verifyHash(archiveFile, bundle.getDownloadSha256())) {
                    String errorMessage = "An error occurred during plugin installation: Downloaded file is invalid, corrupted or can't be verified";
                    updateMessage(errorMessage);
                    LOGGER.error(errorMessage);
                    //noinspection ResultOfMethodCallIgnored
                    archiveFile.delete();
                    updateProgress(1, 1);
                    throw new TaskException(errorMessage);
                }
            }

            commitProgress(100);
            updateMessage("Installing plugin " + bundle.getRemotePackage().getName() + " - Extracting files...");
            File extractedArchiveFolder = new File(ApplicationDefaults.getTempDownloadDirectory() + File.separator + "temp-"
                    + archiveFile.getName().replace(".owlpack", ""));
            ArchiveUtils.extract(archiveFile.getAbsolutePath(), extractedArchiveFolder.getAbsolutePath());

            commitProgress(30);

            updateMessage("Installing plugin " + bundle.getRemotePackage().getName() + " - Moving files...");
            installToPluginDirectory(extractedArchiveFolder, targetDirectory);

            commitProgress(20);

            updateMessage("Installing plugin " + bundle.getRemotePackage().getName() + " - Cleaning files...");
            if (!archiveFile.delete()) {
                LOGGER.warn("For some reason the files could not be deleted at {}", archiveFile.getPath());
            }
            FileUtils.deleteDirectory(extractedArchiveFolder);

            commitProgress(10);
            updateMessage("Plugin " + bundle.getRemotePackage().getName() + " successfully Installed");

        } catch (IOException e) {
            updateMessage("An error occurred during plugin install: " + e.getMessage());
            LOGGER.error("An error occurred during plugin install: {}", e.getMessage());
            updateProgress(1, 1);
            throw new TaskException("An error occurred during plugin install", e);
        }

        return completed();
    }

    private File downloadInTempDirectory(PackageBundle bundle) throws TaskException {

        URL website;
        try {
            website = URI.create(bundle.getDownloadUrl()).toURL();
        } catch (MalformedURLException e) {
            updateMessage("Installation of " + bundle.getRemotePackage().getName() + " canceled: Can't download plugin files");
            throw new TaskException(e);
        }

        final var horoDateFormat = new SimpleDateFormat("ddMMyyhhmmssSSS");
        final var tempDownloadDir = new File(ApplicationDefaults.getTempDownloadDirectory());
        if (!tempDownloadDir.mkdirs()) {
            LOGGER.warn("The following directory could not be created: {}", tempDownloadDir.getPath());
        }

        String outPutFileName = horoDateFormat.format(new Date()) + ".owlpack";
        String outputFilePath = ApplicationDefaults.getTempDownloadDirectory() + File.separator + outPutFileName;
        File outputFile = new File(outputFilePath);

        try (
                CallbackByteChannel rbc = new CallbackByteChannel(Channels.newChannel(website.openStream()),
                        contentLength(website));
                FileOutputStream fos = new FileOutputStream(outputFile)) {

            rbc.setCallback(this::computeTotalProgress);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            return outputFile;

        } catch (MalformedURLException e) {
            updateMessage("Installation of " + bundle.getRemotePackage().getName() + " canceled: Can't download plugin files");
            throw new TaskException(e);
        } catch (FileNotFoundException e) {
            updateMessage("Installation of " + bundle.getRemotePackage().getName() + " canceled: File not found");
            throw new TaskException(e);
        } catch (IOException e) {
            updateMessage("Installation of " + bundle.getRemotePackage().getName() + " canceled: Can't write file on disk");
            throw new TaskException(e);
        }

    }

    private void installToPluginDirectory(File source, File target) throws IOException {

        OwlPackStructureType structure = getStructureType(source);
        // Choose the folder to copy from the downloaded source
        File newSource = source;
        switch (structure) {
            case NESTED -> newSource = requireNonNull(source.listFiles())[0];
            case ENV -> newSource = getSubFileByPlatformTag(source);
            case NESTED_ENV -> newSource = getSubFileByPlatformTag(requireNonNull(source.listFiles())[0]);
            default -> LOGGER.debug("Can't determine owlpack structure type (NESTED, ENV or NESTED_ENV)."
                    + " Directory will be used as it.");
        }

        FileUtils.copyDirectory(newSource, target);
    }

    private OwlPackStructureType getStructureType(File directory) {

        RuntimePlatform runtimePlatform = applicationDefaults.getRuntimePlatform();
        OwlPackStructureType structure = OwlPackStructureType.DIRECT;

        final var filesList = requireNonNull(directory.listFiles());

        if (filesList.length == 1 && filesList[0].isDirectory() && !runtimePlatform.getCompatiblePlatformsTags().contains(filesList[0].getName())) {
            structure = OwlPackStructureType.NESTED;
            final var innerFilesList = requireNonNull(filesList[0].listFiles());
            for (final var file : innerFilesList) {
                if (runtimePlatform.getCompatiblePlatformsTags().contains(file.getName())) {
                    structure = OwlPackStructureType.NESTED_ENV;
                }
            }
        } else if (filesList.length >= 1) {
            // if the directory describes an environment-related bundle
            for (final var file : filesList) {
                if (runtimePlatform.getCompatiblePlatformsTags().contains(file.getName())) {
                    return OwlPackStructureType.ENV;
                }
            }
        }
        return structure;
    }

    private int contentLength(URL url) {
        HttpURLConnection connection;
        int contentLength;
        try {
            connection = (HttpURLConnection) url.openConnection();
            contentLength = connection.getContentLength();
        } catch (Exception e) {
            return 1;
        }
        return contentLength;
    }

    private File getSubFileByPlatformTag(final File parent) {
        RuntimePlatform runtimePlatform = applicationDefaults.getRuntimePlatform();
        final var subFiles = requireNonNull(parent.listFiles());

        for (String platformTag : runtimePlatform.getCompatiblePlatformsTags()) {
            for (final var file : subFiles) {
                if (file.getName().equals(platformTag)) {
                    return file;
                }
            }
        }
        return null;
    }

    private boolean verifyHash(final File file, final String expectedHash) {
        String fileHash;
        try {
            fileHash = CryptoUtils.getFileSha256Digest(file);
        } catch (IOException e) {
            LOGGER.error("File hash can't be computed", e);
            return false;
        }

        if (expectedHash.equalsIgnoreCase(fileHash)) {
            LOGGER.debug("Valid SHA256 given: {}, expected: {}", fileHash, expectedHash);
            return true;
        } else {
            LOGGER.warn("Invalid SHA256 given: {}, expected: {}", fileHash, expectedHash);
            return false;
        }

    }

    /**
     * Compatible package archive structures.
     * <pre>
     * -------------- DIRECT
     * plugin.zip/
     *   ├── plugin.dll
     *   └── (other required files...)
     *
     * -------------- NESTED
     * plugin.zip/
     *   └── plugin
     *         ├── plugin.dll
     *         └── (other required files...)
     *
     * -------------- NESTED_ENV
     * plugin.zip/
     *   └── plugin
     *         ├── x86
     *         │    ├── plugin.dll
     *         │    └── (other required files...)
     *         └── x64
     *              ├── plugin.dll
     *              └── (other required files...)
     * </pre>
     */
    private enum OwlPackStructureType {
        DIRECT,
        ENV,
        NESTED,
        NESTED_ENV,
    }

}
