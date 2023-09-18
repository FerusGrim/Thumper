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
package xyz.ferus.thumper.exchange;

import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.ferus.thumper.queue.TopicQueue;

/**
 * A topic exchange is a named entity that accepts messages from producers and routes them to message queues based on rules. The routing key is matched with wildcards.
 */
public interface TopicExchange extends Exchange {

    /**
     * Publishes a message to this exchange.
     * @param routingKey the routing key to use
     * @param data the data to publish
     * @return a future that completes when the message has been published
     */
    CompletableFuture<@Nullable Void> publish(String routingKey, Object data);

    /**
     * Publishes a message to this exchange.
     * @param routingKey the routing key to use
     * @return a future that completes when the message has been published
     */
    default CompletableFuture<TopicQueue> newQueue(String routingKey) {
        return newQueue(routingKey, new String[0]);
    }

    /**
     * Publishes a message to this exchange.
     * @param routingKey the routing key to use
     * @param otherRoutingKeys other routing keys to use
     * @return a future that completes when the message has been published
     */
    CompletableFuture<TopicQueue> newQueue(String routingKey, String... otherRoutingKeys);
}
