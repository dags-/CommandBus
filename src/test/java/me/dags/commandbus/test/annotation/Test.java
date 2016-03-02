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

package me.dags.commandbus.test.annotation;

import me.dags.commandbus.CommandBus;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Key;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */

@Plugin(name = "test", id = "test")
public class Test
{
    @Listener
    public void init(GameInitializationEvent event)
    {
        new CommandBus(this).register(this).submitCommands();
    }

    @Command(aliases = "c1", perm = "c1.node", desc = "Prints (name) to the user")
    public void command1(@Caller CommandSource player, @Key("name") String name)
    {
        player.sendMessage(Text.of("C1: " + name));
    }

    @Command(aliases = "s1", parent = "c1")
    public void subCommand1(@Caller Player player, @Key("name") String name)
    {
        player.sendMessage(Text.of("SUB1: " + name));
    }

    @Command(aliases = "s2", parent = "c1", desc = "Prints the (int) to the user")
    public void subCommand2(@Caller Player player, @Key("number") int number)
    {
        player.sendMessage(Text.of("SUB2: " + number));
    }

    @Command(aliases = "s3", parent = "c1 s2", desc = "Prints the (string)name & (int)number")
    public void subCommand3(@Caller Player player, @Key("name") String name, @Key("number") int number)
    {
        player.sendMessage(Text.of("SUB3 name:" + name + ", number: " + number));
    }

    @Command(aliases = "s4", parent = "c2 s2")
    public void subCommand4(@Caller Player player, @Key("name") String name, @Key("number") int number)
    {
        player.sendMessage(Text.of("SUB3 name:" + name + ", number: " + number));
    }
}
