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
package xyz.ferus.thumper.codec;

import xyz.ferus.thumper.internal.codec.GzipCodec;

/**
 * A codec is used to encode and decode objects to and from byte arrays.
 * @param <T> the type of object to encode and decode
 */
public interface Codec<T> {

    /**
     * Get the type of object this codec encodes and decodes.
     * @return the type of object this codec encodes and decodes
     */
    Class<T> type();

    /**
     * Encode an object to a byte array.
     * @param object the object to encode
     * @return the encoded object
     * @throws EncodingException if the object could not be encoded
     */
    byte[] encode(T object) throws EncodingException;

    /**
     * Decode a byte array to an object.
     * @param bytes the byte array to decode
     * @return the decoded object
     * @throws EncodingException if the byte array could not be decoded
     */
    T decode(byte[] bytes) throws EncodingException;

    /**
     * Wrap a codec in a gzip codec.
     * <p>
     * If the codec is already a gzip codec, it will be returned as-is.
     * @param codec the codec to wrap
     * @return the gzip codec (or the codec if it is already a gzip codec)
     * @param <T> the type of object to encode and decode
     */
    static <T> Codec<T> gzip(Codec<T> codec) {
        return codec instanceof GzipCodec ? codec : new GzipCodec<>(codec);
    }
}
