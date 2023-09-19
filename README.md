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
        // Create a Rabbit instance with the default configuration (and pooled channels)
        RabbitBuilder builder = Rabbit.builder().pooled();
        Rabbit rabbit = builder.build().join();

        // Register the codec for LoggingMessage
        rabbit.codecs().register(new LoggingMessageCodec());

        // Create a direct exchange named "logging"
        DirectExchange logging = rabbit.direct("logging").join();

        // Create a queue routed to the "info" key on the "logging" exchange
        DirectQueue infoQueue = logging.newQueue("info").join();

        // Subscribe to the info queue and print incoming messages
        Subscription subscription = infoQueue.subscribe(LoggingMessage.class, message -> System.out.println("[INFO] " + message.message())).join();

        // Publish a message to the logging exchange routed to the "info" key
        logging.publish("info", new LoggingMessage("Hello World!")).join();

        // All done!
        subscription.close();
        infoQueue.close();
        logging.close();
        rabbit.close();
    }

    public record LoggingMessage(String message) {}

    public static class LoggingMessageCodec implements Codec<LoggingMessage> {

        @Override
        public Class<LoggingMessage> type() {
            return LoggingMessage.class;
        }

        @Override
        public byte[] encode(LoggingMessage object) {
            return object.message().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public LoggingMessage decode(byte[] bytes) {
            return new LoggingMessage(new String(bytes, StandardCharsets.UTF_8));
        }
    }
}
```
