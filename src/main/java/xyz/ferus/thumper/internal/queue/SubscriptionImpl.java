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
import java.lang.ref.WeakReference;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.ferus.thumper.internal.AbstractRabbitImpl;
import xyz.ferus.thumper.queue.Subscription;

public class SubscriptionImpl implements Subscription {

    private final AbstractRabbitImpl rabbit;
    private final AbstractQueueImpl queue;

    private WeakReference<Channel> channel;
    private String consumerTag;

    public SubscriptionImpl(AbstractRabbitImpl rabbit, AbstractQueueImpl queue, Channel channel, String consumerTag) {
        this.rabbit = rabbit;
        this.queue = queue;
        this.channel = new WeakReference<>(channel);
        this.consumerTag = consumerTag;
    }

    public void channelChanged(Channel newChannel, String newConsumerTag) {
        this.channel = new WeakReference<>(newChannel);
        this.consumerTag = newConsumerTag;
    }

    @Override
    public String consumerTag() {
        return this.consumerTag;
    }

    @Override
    public void close() throws Exception {
        this.queue.removeSubscription(this.consumerTag);

        @Nullable Channel channel = this.channel.get();
        if (channel != null && channel.isOpen()) {
            channel.basicCancel(this.consumerTag);
        }
    }
}
