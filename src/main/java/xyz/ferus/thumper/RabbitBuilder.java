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
package xyz.ferus.thumper;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.ferus.thumper.codec.CodecRegistry;
import xyz.ferus.thumper.internal.RabbitImpl;
import xyz.ferus.thumper.internal.RabbitImplPooled;
import xyz.ferus.thumper.internal.SharedThreadFactory;

/**
 * A builder for a Rabbit instance.
 */
public class RabbitBuilder {

    /**
     * The default thread factory.
     */
    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new SharedThreadFactory();

    /**
     * The connection factory.
     */
    private ConnectionFactory connectionFactory = new ConnectionFactory();

    /**
     * The thread factory.
     */
    private ThreadFactory threadFactory = DEFAULT_THREAD_FACTORY;

    /**
     * The codec registry.
     */
    private CodecRegistry codecRegistry = CodecRegistry.defaultRegistry();

    /**
     * The channel pool config.
     */
    @Nullable private GenericObjectPoolConfig<Channel> channelPoolConfig = null;

    RabbitBuilder() {}

    /**
     * Sets the connection factory.
     *
     * @param connectionFactory the connection factory
     * @return this builder
     */
    public RabbitBuilder connection(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }

    /**
     * Sets the thread factory.
     *
     * @param threadFactory the thread factory
     * @return this builder
     */
    public RabbitBuilder threads(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    /**
     * Sets the channel pool config to the default config.
     *
     * @return this builder
     */
    public RabbitBuilder pooled() {
        return pooled(new GenericObjectPoolConfig<>());
    }

    /**
     * Sets the channel pool config.
     *
     * @param channelPoolConfig the channel pool config
     * @return this builder
     */
    public RabbitBuilder pooled(GenericObjectPoolConfig<Channel> channelPoolConfig) {
        this.channelPoolConfig = channelPoolConfig;
        return this;
    }

    /**
     * Sets the codec registry.
     *
     * @param codecRegistry the codec registry
     * @return this builder
     */
    public RabbitBuilder codecs(CodecRegistry codecRegistry) {
        this.codecRegistry = codecRegistry;
        return this;
    }

    /**
     * Builds a Rabbit instance.
     * @return a Rabbit instance
     */
    public CompletableFuture<Rabbit> build() {
        ExecutorService executor = Executors.newCachedThreadPool(this.threadFactory);
        if (this.channelPoolConfig != null) {
            this.channelPoolConfig.setTestOnReturn(true);
            this.channelPoolConfig.setTestOnBorrow(true);
            this.channelPoolConfig.setTestWhileIdle(true);
        }

        CompletableFuture<Rabbit> future = new CompletableFuture<>();
        executor.execute(() -> {
            Connection connection;
            try {
                connection = this.connectionFactory.newConnection();
            } catch (IOException | TimeoutException e) {
                future.completeExceptionally(e);
                return;
            }

            if (this.channelPoolConfig != null) {
                future.complete(new RabbitImplPooled(connection, executor, this.codecRegistry, this.channelPoolConfig));
            } else {
                future.complete(new RabbitImpl(connection, executor, this.codecRegistry));
            }
        });
        return future;
    }
}
