# Thumper
A simple, lightweight wrapper for the RabbitMQ Java client library.

## Usage

```java
import java.nio.charset.StandardCharsets;
import xyz.ferus.thumper.Rabbit;
import xyz.ferus.thumper.RabbitBuilder;
import xyz.ferus.thumper.codec.Codec;
import xyz.ferus.thumper.exchange.DirectExchange;
import xyz.ferus.thumper.queue.DirectQueue;
import xyz.ferus.thumper.queue.Subscription;

public class Example {

    public static void main(String[] args) throws Exception {
        RabbitBuilder builder = Rabbit.builder().pooled();

        Rabbit rabbit = builder.build().join();
        rabbit.codecs().register(new SimpleMessageCodec());

        DirectExchange exchange = rabbit.direct("direct_exchange").join();
        DirectQueue queue = exchange.newQueue("direct_queue").join();

        Subscription subscription = queue.subscribe(SimpleMessage.class, message -> System.out.println("Message received: " + message.message())).join();
        exchange.publish("direct_queue", new SimpleMessage("Hello, world!")).join();

        subscription.close();
        queue.close();
        exchange.close();
        rabbit.close();
    }

    public record SimpleMessage(String message) {}

    public static class SimpleMessageCodec implements Codec<SimpleMessage> {

        @Override
        public Class<SimpleMessage> type() {
            return SimpleMessage.class;
        }

        @Override
        public byte[] encode(SimpleMessage object) {
            return object.message().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public SimpleMessage decode(byte[] bytes) {
            return new SimpleMessage(new String(bytes, StandardCharsets.UTF_8));
        }
    }
}
```
