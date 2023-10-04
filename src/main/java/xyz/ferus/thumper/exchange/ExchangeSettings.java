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
package xyz.ferus.thumper.exchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Settings for an exchange.
 * @param durable whether the exchange should survive a broker restart
 * @param autoDelete whether the exchange should be deleted when no longer in use
 * @param arguments additional arguments for the exchange
 */
public record ExchangeSettings(boolean durable, boolean autoDelete, Map<String, Object> arguments) {

    private static final ExchangeSettings DEFAULT = new ExchangeSettings(false, false, Collections.emptyMap());

    /**
     * Returns the default settings for an exchange.
     * @return the default settings
     */
    public static ExchangeSettings defaultSettings() {
        return DEFAULT;
    }

    /**
     * Returns a builder for creating exchange settings.
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for creating exchange settings.
     */
    public static class Builder {

        /**
         * Whether the exchange should survive a broker restart.
         */
        private boolean durable;

        /**
         * Whether the exchange should be deleted when no longer in use.
         */
        private boolean autoDelete;

        /**
         * Additional arguments for the exchange.
         */
        private Map<String, Object> arguments;

        private Builder() {
            this.durable = false;
            this.autoDelete = false;
            this.arguments = new HashMap<>();
        }

        /**
         * Sets whether the exchange should survive a broker restart.
         * @param durable whether the exchange should survive a broker restart
         * @return this builder
         */
        public Builder durable(boolean durable) {
            this.durable = durable;
            return this;
        }

        /**
         * Sets whether the exchange should be deleted when no longer in use.
         * @param autoDelete whether the exchange should be deleted when no longer in use
         * @return this builder
         */
        public Builder autoDelete(boolean autoDelete) {
            this.autoDelete = autoDelete;
            return this;
        }

        /**
         * Sets additional arguments for the exchange.
         * @param arguments additional arguments for the exchange
         * @return this builder
         */
        public Builder arguments(Map<String, Object> arguments) {
            this.arguments = arguments;
            return this;
        }

        /**
         * Sets an additional argument for the exchange.
         * @param key the key of the argument
         * @param value the value of the argument
         * @return this builder
         */
        public Builder set(String key, Object value) {
            this.arguments.put(key, value);
            return this;
        }

        /**
         * Builds the exchange settings.
         * @return the exchange settings
         */
        public ExchangeSettings build() {
            return new ExchangeSettings(this.durable, this.autoDelete, Map.copyOf(this.arguments));
        }
    }
}
