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
import java.util.concurrent.Executor;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import xyz.ferus.thumper.RabbitException;
import xyz.ferus.thumper.codec.CodecRegistry;

public class RabbitImplPooled extends AbstractRabbitImpl implements PooledObjectFactory<Channel> {

    private final GenericObjectPool<Channel> channelPool;

    public RabbitImplPooled(
            Connection connection,
            Executor executor,
            CodecRegistry codecRegistry,
            GenericObjectPoolConfig<Channel> poolConfig) {
        super(connection, executor, codecRegistry);
        this.channelPool = new GenericObjectPool<>(this, poolConfig);
    }

    @Override
    protected Channel provideChannel() throws RabbitException {
        try {
            return this.channelPool.borrowObject();
        } catch (Exception e) {
            throw new RabbitException("Encountered an error while creating a Rabbit channel.", e);
        }
    }

    @Override
    protected void closeInternal() {
        this.channelPool.close();
    }

    @Override
    public void activateObject(PooledObject<Channel> p) {}

    @Override
    public void destroyObject(PooledObject<Channel> p) throws Exception {
        p.getObject().close();
    }

    @Override
    public PooledObject<Channel> makeObject() throws Exception {
        Channel channel = this.connection().createChannel();
        return new DefaultPooledObject<>(channel);
    }

    @Override
    public void passivateObject(PooledObject<Channel> p) {}

    @Override
    public boolean validateObject(PooledObject<Channel> p) {
        return p.getObject().isOpen();
    }
}
