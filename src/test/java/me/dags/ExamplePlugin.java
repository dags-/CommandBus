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

package me.dags;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.annotation.*;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.util.Collection;

/**
 * @author dags <dags@dags.me>
 */

@Plugin(name = "ExamplePlugin", id = "ExamplePlugin")
public class ExamplePlugin
{
    @Listener
    public void init(GameInitializationEvent event)
    {
        CommandBus bus = new CommandBus();
        bus.register(this);
        bus.submit(this);
    }

    @Command(aliases = "pm", perm = "ExamplePlugin.pm.send", desc = "Send a private message to someone")
    public void message(@Caller CommandSource from, @One Player to, @Join String message)
    {
        from.sendMessage(Text.of("You -> " + to.getName() + ": " + message));
        to.sendMessage(Text.of("" + from.getName() + " -> You: " + message));
    }

    @Command(aliases = "pma", perm = "ExamplePlugin.pm.send", desc = "Send a private to all those whose name starts with <to>")
    public void messageAll(@Caller CommandSource from, @All Collection<Player> to, @Join String message)
    {
        from.sendMessage(Text.of("You -> ToAll: " + message));
        to.stream().filter(p -> p != from).forEach(p -> p.sendMessage(Text.of("" + from.getName() + " -> You: " + message)));
    }

    @Command(aliases = "test")
    public void test(@Caller CommandSource from, @Join String message)
    {
        from.sendMessage(Text.of("TEST: " + message));
    }

    @Command(aliases = "a", parent = "test")
    public void test2(@Caller CommandSource from, @Join String message)
    {
        from.sendMessage(Text.of("TEST A: " + message));
    }

    @Command(aliases = "b", parent = "test a")
    public void test3(@Caller CommandSource from, @Join String message)
    {
        from.sendMessage(Text.of("TEST A B : " + message));
    }

    @Command(aliases = "c", parent = "chest y")
    public void test3a(@Caller CommandSource from, @One int message, @Join String string)
    {
        from.sendMessage(Text.of("TEST A BB : " + message + ":" + string));
    }

    @Command(aliases = "c", parent = "anothertest")
    public void test4(@Caller CommandSource from, @Join String message)
    {
        from.sendMessage(Text.of("TEST C: " + message));
    }
}
