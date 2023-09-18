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
package xyz.ferus.thumper.internal.util;

import java.util.LinkedList;
import java.util.List;

public class ExceptionalHandler {

    private final List<ExceptionalRunnable> tasks;

    public ExceptionalHandler() {
        this.tasks = new LinkedList<>();
    }

    public void add(ExceptionalRunnable task) {
        this.tasks.add(task);
    }

    public void execute() throws CompositeException {
        List<Exception> exceptions = new LinkedList<>();
        for (ExceptionalRunnable task : this.tasks) {
            try {
                task.run();
            } catch (Exception t) {
                exceptions.add(t);
            }
        }

        if (!exceptions.isEmpty()) {
            throw new CompositeException(exceptions);
        }
    }
}
