/*
 * MIT License
 *
 * Copyright (c) 2023 Nicholas Badger (FerusGrim) <https://ferus.xyz>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.ferus.thumper.internal.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import xyz.ferus.thumper.codec.Codec;
import xyz.ferus.thumper.codec.EncodingException;

public class GzipCodec<T> implements Codec<T> {

    private final Codec<T> delegate;

    public GzipCodec(Codec<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Class<T> type() {
        return this.delegate.type();
    }

    @Override
    public byte[] encode(T object) throws EncodingException {
        byte[] encoded = this.delegate.encode(object);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut)) {
            gzipOut.write(encoded);
        } catch (Exception e) {
            throw new EncodingException("Encountered an error while compressing the encoded object.", e);
        }
        return byteOut.toByteArray();
    }

    @Override
    public T decode(byte[] bytes) throws EncodingException {
        byte[] uncompressed;
        try (GZIPInputStream gzipIn = new GZIPInputStream(new java.io.ByteArrayInputStream(bytes))) {
            uncompressed = toByteArray(gzipIn);
        } catch (Exception e) {
            throw new EncodingException("Encountered an error while decompressing the encoded object.", e);
        }
        return this.delegate.decode(uncompressed);
    }

    // ByteStreams.toByteArray(InputStream)
    // https://github.com/google/guava/blob/master/guava/src/com/google/common/io/ByteStreams.java
    // https://github.com/google/guava/blob/master/guava/src/com/google/common/math/IntMath.java
    // https://github.com/google/guava/blob/master/guava/src/com/google/common/primitives/Ints.java
    private static final int BUFFER_SIZE = 8192;
    private static final int TO_BYTE_ARRAY_DEQUE_SIZE = 20;
    private static final int MAX_ARRAY_LEN = Integer.MAX_VALUE - 8;

    public static byte[] toByteArray(InputStream in) throws IOException {
        return toByteArrayInternal(in, new ArrayDeque<>(TO_BYTE_ARRAY_DEQUE_SIZE));
    }

    private static byte[] toByteArrayInternal(InputStream in, Queue<byte[]> buffers) throws IOException {
        int totalLen = 0;
        int initialBufferSize = Math.min(BUFFER_SIZE, Math.max(128, Integer.highestOneBit(totalLen) * 2));
        for (int bufSize = initialBufferSize;
                totalLen < MAX_ARRAY_LEN;
                bufSize = saturatedMultiply(bufSize, bufSize < 4096 ? 4 : 2)) {
            byte[] buf = new byte[Math.min(bufSize, MAX_ARRAY_LEN - totalLen)];
            buffers.add(buf);
            int off = 0;
            while (off < buf.length) {
                int r = in.read(buf, off, buf.length - off);
                if (r == -1) {
                    return combineBuffers(buffers, totalLen);
                }
                off += r;
                totalLen += r;
            }
        }

        if (in.read() == -1) {
            return combineBuffers(buffers, MAX_ARRAY_LEN);
        } else {
            throw new OutOfMemoryError("input is too large to fit in a byte array");
        }
    }

    private static byte[] combineBuffers(Queue<byte[]> buffers, int totalLen) {
        if (buffers.isEmpty()) {
            return new byte[0];
        }
        byte[] result = buffers.remove();
        if (result.length == totalLen) {
            return result;
        }
        int remaining = totalLen - result.length;
        result = Arrays.copyOf(result, totalLen);
        while (remaining > 0) {
            byte[] buf = buffers.remove();
            int bytesToCopy = Math.min(remaining, buf.length);
            int resultOffset = totalLen - remaining;
            System.arraycopy(buf, 0, result, resultOffset, bytesToCopy);
            remaining -= bytesToCopy;
        }
        return result;
    }

    public static int saturatedMultiply(int a, int b) {
        return saturatedCast((long) a * b);
    }

    public static int saturatedCast(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }
}
