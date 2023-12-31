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

import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.ferus.thumper.codec.CodecRegistry;
import xyz.ferus.thumper.exchange.DirectExchange;
import xyz.ferus.thumper.exchange.FanoutExchange;
import xyz.ferus.thumper.exchange.TopicExchange;

/**
 * A Rabbit instance is the entry point to the RabbitMQ server.
 */
public interface Rabbit extends AutoCloseable {

    /**
     * Executes the given function on the RabbitMQ channel.
     * @param function the function to execute
     * @return a future that completes with the result of the function
     * @param <R> the type of the result
     */
    <R> CompletableFuture<R> transform(ChannelFunction<R> function);

    /**
     * Executes the given consumer on the RabbitMQ channel.
     * @param consumer the consumer to execute
     * @return a future that completes when the consumer has finished
     */
    CompletableFuture<@Nullable Void> execute(ChannelConsumer consumer);

    /**
     * Creates a new {@link DirectExchange} with the given name.
     * @param name the name of the exchange
     * @return the exchange
     */
    DirectExchange direct(String name);

    /**
     * Creates a new {@link TopicExchange} with the given name.
     * @param name the name of the exchange
     * @return the exchange
     */
    TopicExchange topic(String name);

    /**
     * Creates a new {@link FanoutExchange} with the given name.
     * @param name the name of the exchange
     * @return the exchange
     */
    FanoutExchange fanout(String name);

    /**
     * Gets the {@link CodecRegistry} for this Rabbit instance.
     * @return the codec registry for this Rabbit instance
     */
    CodecRegistry codecs();

    /**
     * Creates a {@link RabbitBuilder} for a Rabbit instance.
     * @return a builder for a Rabbit instance
     */
    static RabbitBuilder builder() {
        return new RabbitBuilder();
    }
}
