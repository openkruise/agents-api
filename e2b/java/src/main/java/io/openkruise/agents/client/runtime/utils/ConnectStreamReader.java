package io.openkruise.agents.client.runtime.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Connect Protocol Server-Streaming frame parser.
 *
 * @param <T> protobuf message type
 */
public class ConnectStreamReader<T extends Message> implements Iterator<T>, MessageStream<T> {
    private static final int FLAG_TRAILER = 0x02;
    private static final int HEADER_SIZE = 5;
    private static final int MAX_FRAME_SIZE = 32 * 1024 * 1024;

    private final InputStream inputStream;
    private final Parser<T> parser;
    private T nextMessage;
    private boolean done;
    private boolean closed;
    private String trailerError;
    private IOException lastError;

    public ConnectStreamReader(InputStream inputStream, Parser<T> parser) {
        this.inputStream = inputStream;
        this.parser = parser;
    }

    @Override
    public boolean hasNext() {
        if (done || closed) {
            return false;
        }
        if (nextMessage != null) {
            return true;
        }
        try {
            nextMessage = readNextFrame();
            return nextMessage != null;
        } catch (IOException e) {
            lastError = e;
            done = true;
            return false;
        }
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more messages in stream");
        }
        T msg = nextMessage;
        nextMessage = null;
        return msg;
    }

    private T readNextFrame() throws IOException {
        byte[] header = new byte[HEADER_SIZE];
        if (readFully(header, 0, HEADER_SIZE) < HEADER_SIZE) {
            done = true;
            return null;
        }

        int flags = header[0] & 0xFF;
        int length = ((header[1] & 0xFF) << 24)
            | ((header[2] & 0xFF) << 16)
            | ((header[3] & 0xFF) << 8)
            | (header[4] & 0xFF);

        if (length < 0 || length > MAX_FRAME_SIZE) {
            throw new IOException("Invalid frame length: " + length);
        }

        byte[] payload = new byte[length];
        if (readFully(payload, 0, length) < length) {
            throw new IOException("Unexpected end of stream: expected " + length + " bytes");
        }

        // Trailer frame indicates end of stream
        if ((flags & FLAG_TRAILER) != 0) {
            done = true;
            parseTrailerError(payload);
            return null;
        }

        try {
            return parser.parseFrom(payload);
        } catch (InvalidProtocolBufferException e) {
            throw new IOException("Failed to parse protobuf message", e);
        }
    }

    private void parseTrailerError(byte[] payload) {
        if (payload.length == 0) {
            return;
        }
        String trailerJson = new String(payload, StandardCharsets.UTF_8);
        // Connect Protocol trailer: {"error":{"code":"...","message":"..."}}
        if (trailerJson.contains("\"error\"")) {
            trailerError = trailerJson;
        }
    }

    private int readFully(byte[] buf, int off, int len) throws IOException {
        int total = 0;
        while (total < len) {
            int n = inputStream.read(buf, off + total, len - total);
            if (n < 0) {
                break;
            }
            total += n;
        }
        return total;
    }

    /**
     * Error message from trailer frame, available after hasNext() returns false.
     */
    public String getTrailerError() {
        return trailerError;
    }

    /**
     * If hasNext() returns false due to an IO exception, this method can retrieve it.
     */
    public IOException getLastError() {
        return lastError;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            done = true;
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }
}
