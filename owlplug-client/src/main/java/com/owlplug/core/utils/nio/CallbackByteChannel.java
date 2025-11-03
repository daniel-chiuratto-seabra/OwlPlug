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

package com.owlplug.core.utils.nio;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class CallbackByteChannel implements ReadableByteChannel {
    private final ReadableByteChannel readableByteChannel;
    private final long size;

    @Getter @Setter private ProgressCallback callback;
    private long sizeRead;

    public CallbackByteChannel(final ReadableByteChannel readableByteChannel, final long expectedSize) {
        this.size = expectedSize;
        this.readableByteChannel = readableByteChannel;
    }

    public void close() throws IOException {
        readableByteChannel.close();
    }

    public boolean isOpen() {
        return readableByteChannel.isOpen();
    }

    public int read(ByteBuffer bb) throws IOException {
        int n;
        double progress;
        if ((n = readableByteChannel.read(bb)) > 0) {
            sizeRead += n;
            progress = size > 0 ? (double) sizeRead / (double) size * 100.0 : -1.0;
            callback.onProgress(progress);
        }
        return n;
    }

}
