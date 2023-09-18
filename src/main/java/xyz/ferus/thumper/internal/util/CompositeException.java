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

import java.io.PrintStream;
import java.util.List;

public class CompositeException extends Exception {

    private final List<Exception> exceptions;

    public CompositeException(List<Exception> exceptions) {
        this.exceptions = exceptions;
    }

    @Override
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        for (Exception exception : this.exceptions) {
            s.println("Nested exception: ");
            exception.printStackTrace(s);
        }
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        for (Exception exception : this.exceptions) {
            builder.append(exception.getMessage()).append("\n");
        }
        return builder.toString();
    }
}