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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import xyz.ferus.thumper.exchange.Exchange;
import xyz.ferus.thumper.internal.AbstractRabbitImpl;
import xyz.ferus.thumper.internal.queue.AbstractQueueImpl;
import xyz.ferus.thumper.internal.util.ExceptionalHandler;
import xyz.ferus.thumper.queue.Queue;

public abstract class AbstractExchangeImpl implements Exchange {

    private final AbstractRabbitImpl rabbit;
    private final String name;
    private final List<Queue> queues;

    public AbstractExchangeImpl(AbstractRabbitImpl rabbit, String name) {
        this.rabbit = rabbit;
        this.name = name;
        this.queues = new CopyOnWriteArrayList<>();
    }

    public AbstractRabbitImpl rabbit() {
        return this.rabbit;
    }

    @Override
    public String name() {
        return this.name;
    }

    protected <T extends Queue> T registerQueue(T queue) {
        this.queues.add(queue);
        return queue;
    }

    public void removeQueue(AbstractQueueImpl abstractQueue) {
        this.queues.remove(abstractQueue);
    }

    @Override
    public void close() throws Exception {
        ExceptionalHandler handler = new ExceptionalHandler();

        List<Queue> queues = new ArrayList<>(this.queues);
        this.queues.clear();
        queues.forEach(queue -> handler.add(queue::close));

        handler.add(() -> this.rabbit.close(this));
        handler.execute();
    }

    protected String[] joinRoutingKeys(String routingKey, String... otherRoutingKeys) {
        String[] routingKeys = new String[otherRoutingKeys.length + 1];
        routingKeys[0] = routingKey;
        System.arraycopy(otherRoutingKeys, 0, routingKeys, 1, otherRoutingKeys.length);
        return routingKeys;
    }
}
