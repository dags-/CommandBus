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

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */

public class SpongeCommandBase
{
    private final String[] aliases;
    private final String parentPath;
    private final CommandPath path;
    protected final Set<SpongeCommandBase> children = new LinkedHashSet<>();

    protected SpongeCommandBase parent = null;

    public SpongeCommandBase(String parent, String... alias)
    {
        this.aliases = alias;
        this.parentPath = parent;
        this.path = new CommandPath(parent);
    }

    public boolean isMain()
    {
        return parentPath.isEmpty();
    }

    public CommandPath path()
    {
        return path;
    }

    public String command()
    {
        return isMain() ? alias() : parentPath + " " + alias();
    }

    public String alias()
    {
        return aliases[0];
    }

    public String[] aliases()
    {
        return aliases;
    }

    public SpongeCommandBase addChild(SpongeCommandBase child)
    {
        children.add(child);
        child.parent = this;
        return this;
    }

    public CommandSpec spec()
    {
        CommandSpec.Builder builder = CommandSpec.builder();

        Text.Builder extendedInfo = Text.builder();
        appendExtendedInfo(extendedInfo);
        Text description = Text.of(this.toString());

        children.forEach(c -> builder.child(c.spec(), c.aliases()));
        builder.extendedDescription(extendedInfo.build());
        builder.description(description);

        return builder.build();
    }

    protected void appendExtendedInfo(Text.Builder builder)
    {
        builder.append(Text.of(this));
        children.forEach(c -> {
            builder.append(Text.NEW_LINE);
            c.appendExtendedInfo(builder);
        });
    }

    protected boolean matchFor(String arg, CommandSource source, CommandContext context)
    {
        return false;
    }

    @Override
    public String toString()
    {
        return "/" + (isMain() ? alias() : command()) + " - Command stub";
    }
}
