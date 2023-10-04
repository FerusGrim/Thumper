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
package xyz.ferus.thumper.internal.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.ferus.thumper.codec.Codec;
import xyz.ferus.thumper.codec.EncodingException;
import xyz.ferus.thumper.internal.AbstractRabbitImpl;
import xyz.ferus.thumper.internal.exchange.AbstractExchangeImpl;
import xyz.ferus.thumper.internal.util.ExceptionCatcher;
import xyz.ferus.thumper.queue.Queue;
import xyz.ferus.thumper.queue.QueueConsumer;
import xyz.ferus.thumper.queue.Subscription;

public abstract class AbstractQueueImpl implements Queue {

    private final AbstractRabbitImpl rabbit;
    private final AbstractExchangeImpl exchange;
    private final String name;
    private final Map<String, Subscription> subscriptions;

    public AbstractQueueImpl(AbstractRabbitImpl rabbit, AbstractExchangeImpl exchange, String name) {
        this.rabbit = rabbit;
        this.exchange = exchange;
        this.name = name;
        this.subscriptions = new ConcurrentHashMap<>();
    }

    public AbstractRabbitImpl rabbit() {
        return this.rabbit;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void close() throws Exception {
        ExceptionCatcher catcher = new ExceptionCatcher();

        List<Subscription> subscriptions = new ArrayList<>(this.subscriptions.values());
        this.subscriptions.clear();
        subscriptions.forEach(subscription -> catcher.execute(subscription::close));

        catcher.execute(() -> this.exchange.removeQueue(this));
        catcher.execute(() ->
                this.rabbit.execute(channel -> channel.queueDelete(this.name)).join());
        catcher.validate();
    }

    @SuppressWarnings("resource")
    public void removeSubscription(String consumerTag) {
        this.subscriptions.remove(consumerTag);
    }

    @SuppressWarnings("resource")
    private <T> void cancelCallback(String consumerTag, Class<T> type, QueueConsumer<T> consumer) {
        @Nullable SubscriptionImpl updating = (SubscriptionImpl) this.subscriptions.remove(consumerTag);
        if (updating == null) {
            return;
        }

        this.rabbit
                .execute(channel -> {
                    String newConsumerTag = registerConsumer(channel, type, consumer);
                    updating.channelChanged(channel, newConsumerTag);
                    this.subscriptions.put(newConsumerTag, updating);
                })
                .join();
    }

    @Override
    public <T> CompletableFuture<Subscription> subscribe(Class<T> type, QueueConsumer<T> consumer) {
        return this.rabbit().transform(channel -> {
            String consumerTag = registerConsumer(channel, type, consumer);
            SubscriptionImpl subscription = new SubscriptionImpl(this.rabbit, this, channel, consumerTag);
            this.subscriptions.put(consumerTag, subscription);
            return subscription;
        });
    }

    private <T> String registerConsumer(Channel channel, Class<T> type, QueueConsumer<T> consumer)
            throws EncodingException, IOException {
        Codec<T> codec = this.rabbit.codecs().get(type);
        DeliverCallbackImpl<T> callback = new DeliverCallbackImpl<>(codec, consumer);
        return channel.basicConsume(this.name(), callback, removing -> cancelCallback(removing, type, consumer));
    }

    public static class DeliverCallbackImpl<T> implements DeliverCallback {
        private final Codec<T> codec;
        private final QueueConsumer<T> consumer;

        public DeliverCallbackImpl(Codec<T> codec, QueueConsumer<T> consumer) {
            this.codec = codec;
            this.consumer = consumer;
        }

        @Override
        public void handle(String consumerTag, Delivery message) {
            T decoded;
            try {
                byte[] encoded = message.getBody();
                decoded = this.codec.decode(encoded);
            } catch (Exception e) {
                throw new RuntimeException("Failed to decode message", e);
            }

            try {
                this.consumer.accept(decoded);
            } catch (Exception e) {
                throw new RuntimeException("Failed to consume message", e);
            }
        }
    }
}
