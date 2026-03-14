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

package com.owlplug;

import javafx.application.Preloader.PreloaderNotification;
import lombok.Getter;

import java.util.Objects;

/**
 * Represents a progress message notification used by the JavaFX Preloader.
 * This class encapsulates information about the current progress, a descriptive message,
 * and the status (done or failed) of the application's initialization phase.
 * It implements {@link javafx.application.Preloader.PreloaderNotification} to be
 * compatible with the JavaFX Preloader mechanism.
 */
@Getter
public class PreloaderProgressMessage implements PreloaderNotification {

    /**
     * A static instance representing a successfully completed preloader progress.
     * This can be used to signal the preloader that the initialization is done without errors.
     */
    public static final PreloaderProgressMessage SUCCESSFULLY_DONE = new PreloaderProgressMessage(true, false);
    /**
     * A static instance representing a failed preloader progress.
     * This can be used to signal the preloader that the initialization has failed.
     */
    public static final PreloaderProgressMessage FAILED = new PreloaderProgressMessage(false, true);

    private final boolean done;
    private final boolean failed;

    private double progress;
    private String message;
    private String type;


    /**
     * Constructs a new {@code PreloaderProgressMessage} with a specified type, message, and progress.
     * This constructor is typically used to update the preloader with ongoing progress
     * and a descriptive message during the application's startup.
     *
     * @param type The type of the progress message (e.g., "info", "warning", "error").
     * @param message A descriptive message about the current progress or status.
     * @param progress The current progress as a double value, usually between 0.0 and 1.0.
     */
    public PreloaderProgressMessage(final String type, final String message, final double progress) {
        this.type = type;
        this.progress = progress;
        this.message = message;
        this.done = false; // Explicitly set to false as this is an in-progress message.
        this.failed = false; // Explicitly set to false as this is an in-progress message.
    }

    /**
     * Constructs a new {@code PreloaderProgressMessage} with a specified type and message,
     * without a specific progress value. This is useful for sending status updates
     * that don't necessarily correspond to a numerical progress.
     *
     * @param type The type of the progress message (e.g., "info", "warning", "error").
     * @param message A descriptive message about the current status.
     */
    public PreloaderProgressMessage(final String type, final String message) {
        this.type = type;
        this.message = message;
        this.done = false; // Explicitly set to false as this is an in-progress message.
        this.failed = false; // Explicitly set to false as this is an in-progress message.
    }

    /**
     * Private constructor used for creating predefined {@code PreloaderProgressMessage} instances
     * like {@code SUCCESSFULLY_DONE} and {@code FAILED}.
     *
     * @param done A boolean indicating if the preloader process is complete.
     * @param failed A boolean indicating if the preloader process has failed.
     */
    private PreloaderProgressMessage(final boolean done, final boolean failed) {
        this.done = done;
        this.failed = failed;
    }

    /**
     * Compares this {@code PreloaderProgressMessage} to the specified object.
     * The result is {@code true} if and only if the argument is not {@code null}
     * and is a {@code PreloaderProgressMessage} object that has the same
     * progress, message, done, and failed status as this object.
     *
     * @param o The object to compare this {@code PreloaderProgressMessage} against.
     * @return {@code true} if the given object represents an equivalent
     *         {@code PreloaderProgressMessage}, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object o) {
        // Check for same object reference.
        if (this == o) {
            return true;
        }
        // Check for null or different class type.
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        // Cast the object to PreloaderProgressMessage.
        PreloaderProgressMessage message1 = (PreloaderProgressMessage) o;
        // Compare all relevant fields for equality.
        return Double.compare(message1.progress, progress) == 0 && done == message1.done && failed == message1.failed
                && Objects.equals(message, message1.message);
    }

    /**
     * Returns a hash code for this {@code PreloaderProgressMessage}.
     * The hash code is computed based on the progress, message, done, and failed status.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        // Compute hash code using all relevant fields.
        return Objects.hash(progress, message, done, failed);
    }

    /**
     * Returns a string representation of this {@code PreloaderProgressMessage}.
     * The string representation includes the progress, message, done, and failed status.
     *
     * @return A string representation of this object.
     */
    @Override
    public String toString() {
        // Construct a string representation including all key fields.
        return "ProgressMessage{" + "progress=" + progress + ", message='" + message + '\'' + ", done=" + done + ", failed="
                + failed + '}';
    }
}
