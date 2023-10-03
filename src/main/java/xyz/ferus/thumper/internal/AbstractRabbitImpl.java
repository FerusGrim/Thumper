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
package xyz.ferus.thumper.internal;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.ferus.thumper.Rabbit;
import xyz.ferus.thumper.RabbitException;
import xyz.ferus.thumper.codec.CodecRegistry;
import xyz.ferus.thumper.exchange.DirectExchange;
import xyz.ferus.thumper.exchange.Exchange;
import xyz.ferus.thumper.exchange.FanoutExchange;
import xyz.ferus.thumper.exchange.TopicExchange;
import xyz.ferus.thumper.internal.exchange.DirectExchangeImpl;
import xyz.ferus.thumper.internal.exchange.FanoutExchangeImpl;
import xyz.ferus.thumper.internal.exchange.TopicExchangeImpl;
import xyz.ferus.thumper.internal.util.CompositeException;
import xyz.ferus.thumper.internal.util.ExceptionCatcher;

public abstract class AbstractRabbitImpl implements Rabbit {

    private final Connection connection;
    private final Executor executor;
    private final CodecRegistry codecRegistry;
    private final List<Exchange> exchanges;

    protected AbstractRabbitImpl(Connection connection, Executor executor, CodecRegistry codecRegistry) {
        this.connection = connection;
        this.executor = executor;
        this.codecRegistry = codecRegistry;
        this.exchanges = new CopyOnWriteArrayList<>();
    }

    public Connection connection() {
        return this.connection;
    }

    @Override
    public CodecRegistry codecs() {
        return this.codecRegistry;
    }

    public CompletableFuture<@Nullable Void> executeChannel(ChannelConsumer consumer) {
        CompletableFuture<@Nullable Void> future = new CompletableFuture<>();
        this.executor.execute(() -> {
            try {
                Channel channel = provideChannel();
                consumer.accept(channel);
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public <R> CompletableFuture<R> transformChannel(ChannelFunction<R> function) {
        CompletableFuture<R> future = new CompletableFuture<>();
        this.executor.execute(() -> {
            try {
                Channel channel = provideChannel();
                R result = function.apply(channel);
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    protected abstract Channel provideChannel() throws RabbitException;

    protected abstract void closeInternal() throws Exception;

    @Override
    public final void close() throws RabbitException {
        ExceptionCatcher catcher = new ExceptionCatcher();

        List<Exchange> exchanges = new ArrayList<>(this.exchanges);
        this.exchanges.clear();
        exchanges.forEach(exchange -> catcher.execute(exchange::close));

        catcher.execute(this::closeInternal);
        catcher.execute(this.connection::close);
        catcher.execute(() -> {
            if (this.executor instanceof ExecutorService service) {
                service.shutdown();
            }
        });

        try {
            catcher.validate();
        } catch (CompositeException e) {
            throw e;
        } catch (Exception e) {
            throw new RabbitException("Exception occurred whilst closing a Rabbit instance: " + e.getMessage(), e);
        }
    }

    public void removeExchange(Exchange exchange) {
        this.exchanges.remove(exchange);
    }

    @Override
    public CompletableFuture<DirectExchange> direct(String name) {
        return transformChannel(channel -> {
            DirectExchangeImpl exchange = new DirectExchangeImpl(this, name);
            channel.exchangeDeclare(name, "direct");
            AbstractRabbitImpl.this.exchanges.add(exchange);
            return exchange;
        });
    }

    @Override
    public CompletableFuture<TopicExchange> topic(String name) {
        return transformChannel(channel -> {
            TopicExchangeImpl exchange = new TopicExchangeImpl(this, name);
            channel.exchangeDeclare(name, "topic");
            AbstractRabbitImpl.this.exchanges.add(exchange);
            return exchange;
        });
    }

    @Override
    public CompletableFuture<FanoutExchange> fanout(String name) {
        return transformChannel(channel -> {
            FanoutExchangeImpl exchange = new FanoutExchangeImpl(this, name);
            channel.exchangeDeclare(name, "fanout");
            AbstractRabbitImpl.this.exchanges.add(exchange);
            return exchange;
        });
    }
}
