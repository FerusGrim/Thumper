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
import java.io.IOException;
import java.util.concurrent.Executor;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import xyz.ferus.thumper.RabbitException;
import xyz.ferus.thumper.codec.CodecRegistry;

public class RabbitImpl extends AbstractRabbitImpl {

    @MonotonicNonNull private Channel channel = null;

    private final Object channelLock = new Object();

    public RabbitImpl(Connection connection, Executor executor, CodecRegistry codecRegistry) {
        super(connection, executor, codecRegistry);
    }

    @Override
    protected Channel provideChannel() throws RabbitException {
        if (this.channel == null || !this.channel.isOpen()) {
            synchronized (this.channelLock) {
                if (this.channel == null || !this.channel.isOpen()) {
                    try {
                        this.channel = this.connection().createChannel();
                    } catch (IOException e) {
                        throw new RabbitException("Encountered an error while creating a Rabbit channel.", e);
                    }
                }
            }
        }
        return this.channel;
    }

    @Override
    protected void closeInternal() throws Exception {
        this.channel.close();
    }
}
