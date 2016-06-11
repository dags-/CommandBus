/*
 * The MIT License (MIT)
 *
 * Copyright (c) dags <https://dags.me>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package me.dags.commandbus.command;

import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.parsing.SingleArg;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class CommandPath {

    private final String raw;
    private final String[] parts;
    private int depth = 0;

    public CommandPath(String raw) {
        this.raw = raw;
        this.parts = raw.split(" ");
    }

    public boolean hasNext() {
        return depth < parts.length;
    }

    public String currentArg() {
        return hasNext() ? parts[depth] : lastArg();
    }

    public String nextArg() {
        return parts[depth++];
    }

    public String lastArg() {
        return depth > 0 ? parts[depth - 1] : parts[0];
    }

    public int remaining() {
        return parts.length - depth;
    }

    private int startPos(int toDepth) {
        int start = 0;
        for (int i = start; i < toDepth; i++) {
            start += parts[i].length() + 1;
        }
        return start;
    }

    public CommandArgs remainingArgs() {
        List<SingleArg> args = new ArrayList<>();
        for (int i = depth; i < parts.length; i++) {
            String part = parts[i];
            int start = startPos(i), end = start + part.length();
            args.add(new SingleArg(part, start, end));
        }
        return new CommandArgs(raw, args);
    }
}
