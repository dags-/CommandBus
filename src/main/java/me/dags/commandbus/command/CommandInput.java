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

import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.parsing.SingleArg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class CommandInput {

    private final String raw;
    private final List<SingleArg> singleArgs;

    private CommandArgs commandArgs;
    private int pos = 0;

    public CommandInput(String input) {
        List<SingleArg> singleArgs = new ArrayList<>();
        for (int pos = 0, start = pos; pos < input.length(); pos++, start = pos) {
            while (pos < input.length() && input.charAt(pos) != ' ') {
                pos++;
            }
            singleArgs.add(new SingleArg(input.substring(start, pos), start, pos));
        }
        this.raw = input;
        this.singleArgs = Collections.unmodifiableList(singleArgs);
        this.commandArgs = new CommandArgs(raw, singleArgs);
    }

    public CommandArgs currentState() {
        return commandArgs;
    }

    public CommandArgs copyState() {
        CommandArgs commandArgs = new CommandArgs(raw, singleArgs);
        commandArgs.setState(this.commandArgs.getState());
        return commandArgs;
    }

    public void next() {
        pos++;
    }

    public int remaining() {
        return singleArgs.size() - pos;
    }
}
