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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.ferus.thumper.codec.Codec;
import xyz.ferus.thumper.codec.CodecFactory;
import xyz.ferus.thumper.codec.CodecRegistry;
import xyz.ferus.thumper.codec.EncodingException;

public class CodecRegistryImpl implements CodecRegistry {

    private static final CodecRegistryImpl DEFAULT_REGISTRY = new CodecRegistryImpl();

    private final Map<Class<?>, Codec<?>> codecs;
    private final List<CodecFactory> factories;

    public CodecRegistryImpl() {
        this.codecs = new ConcurrentHashMap<>();
        this.factories = new CopyOnWriteArrayList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Codec<T> get(Class<T> type) throws EncodingException {
        @Nullable Codec<T> codec = (Codec<T>) this.codecs.get(type);
        if (codec != null) {
            return codec;
        }

        for (CodecFactory factory : this.factories) {
            codec = factory.create(type);
            if (codec != null) {
                return codec;
            }
        }
        throw new EncodingException("No codec found for type " + type.getName());
    }

    @Override
    public <T> void register(Codec<T> codec) {
        this.codecs.put(codec.type(), codec);
    }

    @Override
    public void register(CodecFactory factory) {
        this.factories.add(factory);
    }

    public static CodecRegistry defaultRegistry() {
        return DEFAULT_REGISTRY;
    }
}
