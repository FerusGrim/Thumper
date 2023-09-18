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

import xyz.ferus.thumper.internal.codec.CodecRegistryImpl;

/**
 * A codec registry is used to register and retrieve codecs.
 */
public interface CodecRegistry {

    /**
     * Get a codec for the given type.
     * @param type the type to get a codec for
     * @return the codec
     * @param <T> the type of object to encode and decode
     * @throws EncodingException if a codec could not be found for the given type
     */
    <T> Codec<T> get(Class<T> type) throws EncodingException;

    /**
     * Register a codec for the given type.
     * @param type the type to register a codec for
     * @param codec the codec to register
     * @param <T> the type of object to encode and decode
     */
    <T> void register(Codec<T> codec);

    /**
     * Register a codec factory.
     * @param factory the codec factory to register
     */
    void register(CodecFactory factory);

    /**
     * Create a new codec registry.
     * @return the new codec registry
     */
    static CodecRegistry newRegistry() {
        return new CodecRegistryImpl();
    }

    /**
     * Get the default codec registry.
     * @return the default codec registry
     */
    static CodecRegistry defaultRegistry() {
        return CodecRegistryImpl.defaultRegistry();
    }
}
