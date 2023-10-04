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
package xyz.ferus.thumper.internal.exchange;

import com.rabbitmq.client.BuiltinExchangeType;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.ferus.thumper.codec.Codec;
import xyz.ferus.thumper.exchange.FanoutExchange;
import xyz.ferus.thumper.internal.AbstractRabbitImpl;
import xyz.ferus.thumper.internal.queue.FanoutQueueImpl;
import xyz.ferus.thumper.queue.FanoutQueue;

public class FanoutExchangeImpl extends AbstractExchangeImpl implements FanoutExchange {

    public FanoutExchangeImpl(AbstractRabbitImpl rabbit, String name) {
        super(rabbit, name);
    }

    @Override
    protected BuiltinExchangeType type() {
        return BuiltinExchangeType.FANOUT;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public CompletableFuture<@Nullable Void> publish(Object data) {
        return this.rabbit().execute(channel -> {
            Codec codec = this.rabbit().codecs().get(data.getClass());
            channel.basicPublish(this.name(), "", null, codec.encode(data));
        });
    }

    @Override
    public CompletableFuture<FanoutQueue> newQueue() {
        return this.rabbit().transform(channel -> {
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, this.name(), "");
            return this.registerQueue(new FanoutQueueImpl(this.rabbit(), this, queueName));
        });
    }
}
