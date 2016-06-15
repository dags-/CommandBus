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

package me.dags.commandbus;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Join;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(name = "test", id = "test")
public class TestPlugin {

    private final Logger logger = LoggerFactory.getLogger("TEST");

    @Listener
    public void init(GameInitializationEvent event) {
        CommandBus.newInstance(logger).register(this).submit(this);
    }

    @Command(aliases = "test")
    public void test0(@Caller CommandSource source, User test) {
        source.sendMessage(Text.of("test:" + test.getName()));
    }

    @Command(aliases = "sub", parent = "test")
    public void test2(@Caller CommandSource source, User test) {
        source.sendMessage(Text.of("test:" + test.getName()));
    }

    @Command(aliases = "sub", parent = "tes")
    public void test3(@Caller CommandSource source, User test, String thing) {
        source.sendMessage(Text.of("test:" + test.getName() + " " + thing));
    }

    @Command(aliases = "boop")
    public void test1(@Caller CommandSource source) {
        source.sendMessage(Text.of("boop"));
    }
}
