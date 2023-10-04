# Thumper
A simple, lightweight wrapper for the RabbitMQ Java client library.

## Usage

```java
package xyz.ferus.thumper.example;

import java.nio.charset.StandardCharsets;
import xyz.ferus.thumper.Rabbit;
import xyz.ferus.thumper.RabbitBuilder;
import xyz.ferus.thumper.codec.Codec;
import xyz.ferus.thumper.exchange.DirectExchange;
import xyz.ferus.thumper.queue.DirectQueue;
import xyz.ferus.thumper.queue.Subscription;

public class Example {

    public static void main(String[] args) throws Exception {
        // Create a Rabbit instance with the default configuration, pooled channels, and a blocking executor.
        RabbitBuilder builder = Rabbit.builder().pooled().blockingExecutor();

        // Closing a Rabbit instance will close all exchanges, queues, and subscriptions created by it.
        // But you could, of course, wrap everything in try-with-resources.
        try (Rabbit rabbit = builder.build().join()) {
            // Register the codec for LoggingMessage
            rabbit.codecs().register(new LoggingMessageCodec());

            // Create a direct exchange named "logging"
            DirectExchange logging = rabbit.direct("logging");

            // Declare the exchange with the default settings
            // If the exchange already exists, this is unnecessary
            logging.declare().join();

            // Create a queue routed to the "info" key on the "logging" exchange
            DirectQueue infoQueue = logging.newQueue("info").join();

            // Subscribe to the info queue and print incoming messages
            Subscription subscription = infoQueue
                    .subscribe(LoggingMessage.class, message -> System.out.println("[INFO] " + message.message()))
                    .join();

            // Publish a message to the logging exchange routed to the "info" key
            logging.publish("info", new LoggingMessage("Hello World!")).join();

            // Delete the exchange before closing the Rabbit instance
            // This is separate from the AutoCloseable pattern because it's not a requirement
            logging.delete().join();
        }
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
