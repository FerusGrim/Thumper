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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.ferus.thumper.codec.CodecRegistry;
import xyz.ferus.thumper.internal.DirectExecutor;
import xyz.ferus.thumper.internal.RabbitImpl;
import xyz.ferus.thumper.internal.RabbitImplPooled;
import xyz.ferus.thumper.internal.SharedThreadFactory;

/**
 * A builder for a Rabbit instance.
 */
public class RabbitBuilder {

    /**
     * The connection factory configurator.
     */
    @MonotonicNonNull private Consumer<ConnectionFactory> connectionFactoryConfigurator = null;

    /**
     * The executor.
     */
    @MonotonicNonNull private Executor executor = null;

    /**
     * The codec registry.
     */
    private CodecRegistry codecRegistry = CodecRegistry.defaultRegistry();

    /**
     * The channel pool configurator.
     */
    @MonotonicNonNull private Consumer<GenericObjectPoolConfig<Channel>> channelPoolConfigurator = null;

    RabbitBuilder() {}

    /**
     * Configures the connection factory.
     * @param connectionFactoryConfigurator the connection factory configurator
     * @return this builder
     */
    public RabbitBuilder connection(Consumer<ConnectionFactory> connectionFactoryConfigurator) {
        if (this.connectionFactoryConfigurator == null) {
            this.connectionFactoryConfigurator = connectionFactoryConfigurator;
        } else {
            this.connectionFactoryConfigurator =
                    this.connectionFactoryConfigurator.andThen(connectionFactoryConfigurator);
        }
        return this;
    }

    /**
     * Sets the executor.
     * @param executor the executor
     * @return this builder
     */
    public RabbitBuilder executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Sets the executor service to a blocking executor.
     * @return this builder
     */
    public RabbitBuilder blockingExecutor() {
        return executor(DirectExecutor.INSTANCE);
    }

    /**
     * Sets the channel pool config to the default config.
     *
     * @return this builder
     */
    public RabbitBuilder pooled() {
        initializeChannelPoolConfigurator();
        return this;
    }

    /**
     * Configures the channel pool config.
     * @param channelPoolConfigurator the channel pool configurator
     * @return this builder
     */
    public RabbitBuilder pooled(Consumer<GenericObjectPoolConfig<Channel>> channelPoolConfigurator) {
        initializeChannelPoolConfigurator();
        this.channelPoolConfigurator = this.channelPoolConfigurator.andThen(channelPoolConfigurator);
        return this;
    }

    private void initializeChannelPoolConfigurator() {
        if (this.channelPoolConfigurator == null) {
            this.channelPoolConfigurator = config -> {
                config.setTestOnReturn(true);
                config.setTestOnBorrow(true);
                config.setTestWhileIdle(true);
            };
        }
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
        CompletableFuture<Rabbit> future = new CompletableFuture<>();
        Executor executor = Objects.requireNonNullElseGet(
                this.executor, () -> Executors.newCachedThreadPool(SharedThreadFactory.INSTANCE));
        executor.execute(() -> {
            try {
                Rabbit compiled = buildRabbitInternal(executor);
                future.complete(compiled);
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private Rabbit buildRabbitInternal(Executor executor) throws IOException, TimeoutException {
        @Nullable GenericObjectPoolConfig<Channel> channelPoolConfig;
        if (this.channelPoolConfigurator != null) {
            channelPoolConfig = new GenericObjectPoolConfig<>();
            this.channelPoolConfigurator.accept(channelPoolConfig);
        } else {
            channelPoolConfig = null;
        }

        ConnectionFactory connectionFactory = new ConnectionFactory();
        if (this.connectionFactoryConfigurator != null) {
            this.connectionFactoryConfigurator.accept(connectionFactory);
        }

        Connection connection = connectionFactory.newConnection();
        if (channelPoolConfig != null) {
            return new RabbitImplPooled(connection, executor, this.codecRegistry, channelPoolConfig);
        } else {
            return new RabbitImpl(connection, executor, this.codecRegistry);
        }
    }
}
