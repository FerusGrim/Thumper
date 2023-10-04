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

import java.util.concurrent.CompletableFuture;

/**
 * An exchange is a named entity that accepts messages from producers and routes them to message queues based on rules.
 */
public interface Exchange extends AutoCloseable {

    /**
     * The name of this exchange.
     * @return the name of this exchange
     */
    String name();

    /**
     * Declares this exchange with the default settings.
     * @return a future that completes when the exchange has been declared
     */
    default CompletableFuture<Void> declare() {
        return declare(ExchangeSettings.defaultSettings());
    }

    /**
     * Declares this exchange with the given settings.
     * @param settings the settings for this exchange
     * @return a future that completes when the exchange has been declared
     */
    CompletableFuture<Void> declare(ExchangeSettings settings);

    /**
     * Deletes this exchange.
     * @return a future that completes when the exchange has been deleted
     */
    default CompletableFuture<Void> delete() {
        return delete(false);
    }

    /**
     * Deletes this exchange.
     * @param ifUnused whether to delete the exchange if it is unused
     * @return a future that completes when the exchange has been deleted
     */
    CompletableFuture<Void> delete(boolean ifUnused);
}
