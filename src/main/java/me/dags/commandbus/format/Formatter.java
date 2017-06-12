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

package me.dags.commandbus.format;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.action.*;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.title.Title;

import javax.annotation.Nullable;

/**
 * @author dags <dags@dags.me>
 */
public class Formatter implements TextRepresentable {

    private final Format format;
    private final Formatter parent;
    private final Text.Builder builder = Text.builder();

    private boolean empty = true;

    Formatter(Format format) {
        this(null, format);
    }

    private Formatter(Formatter parent, Format format) {
        this.format = format;
        this.parent = parent;
    }

    public boolean isEmpty() {
        return empty;
    }

    public Formatter newLine() {
        if (!empty) {
            append(Text.NEW_LINE);
        }
        return this;
    }

    public Formatter subTitle() {
        if (parent == null) {
            return new Formatter(this, format);
        }
        throw new UnsupportedOperationException("Subtitle Formatter must NOT already have a parent!");
    }

    public Formatter action(TextAction action) {
        if (action instanceof ShiftClickAction) {
            empty = false;
            builder.onShiftClick((ShiftClickAction) action);
        } else if (action instanceof ClickAction) {
            empty = false;
            builder.onClick((ClickAction) action);
        } else if (action instanceof HoverAction) {
            empty = false;
            builder.onHover((HoverAction) action);
        }
        return this;
    }

    public Formatter info(Object input, Object... args) {
        if (args.length > 0) {
            empty = false;
            builder.append(format.infoText(String.format(input.toString(), args)));
        } else {
            empty = false;
            builder.append(format.infoText(input.toString()));
        }
        return this;
    }

    public Formatter subdued(Object input, Object... args) {
        if (args.length > 0) {
            empty = false;
            builder.append(format.subduedText(String.format(input.toString(), args)));
        } else {
            empty = false;
            builder.append(format.subduedText(input.toString()));
        }
        return this;
    }

    public Formatter stress(Object input, Object... args) {
        if (args.length > 0) {
            empty = false;
            builder.append(format.stressText(String.format(input.toString(), args)));
        } else {
            empty = false;
            builder.append(format.stressText(input.toString()));
        }
        return this;
    }

    public Formatter error(Object input, Object... args) {
        if (args.length > 0) {
            empty = false;
            builder.append(format.errorText(String.format(input.toString(), args)));
        } else {
            empty = false;
            builder.append(format.errorText(input.toString()));
        }
        return this;
    }

    public Formatter warn(Object input, Object... args) {
        if (args.length > 0) {
            empty = false;
            builder.append(format.warnText(String.format(input.toString(), args)));
        } else {
            empty = false;
            builder.append(format.warnText(input.toString()));
        }
        return this;
    }

    public Formatter append(Text text) {
        empty = false;
        builder.append(text);
        return this;
    }

    public Text build() {
        return toText();
    }

    public Title title() {
        return title(null, null, null);
    }

    public Title title(int transition) {
        return title(transition, transition, transition);
    }

    public Title title(int fade, int stay) {
        return title(fade, stay, fade);
    }

    public Title title(@Nullable Integer fadeIn, @Nullable Integer stay, @Nullable Integer fadeOut) {
        Text main = parent == null ? this.build() : parent.build();
        Text sub = parent == null ? null : this.build();
        return Title.builder()
                .title(main)
                .subtitle(sub)
                .fadeIn(fadeIn)
                .fadeOut(fadeOut)
                .stay(stay)
                .build();
    }

    public Formatter tell(MessageReceiver receiver) {
        receiver.sendMessage(build());
        return this;
    }

    public Formatter tell(Iterable<? extends MessageReceiver> receivers) {
        Text message = build();
        for (MessageReceiver receiver : receivers) {
            receiver.sendMessage(message);
        }
        return this;
    }

    public Formatter tell(MessageReceiver... receivers) {
        Text message = build();
        for (MessageReceiver receiver : receivers) {
            receiver.sendMessage(message);
        }
        return this;
    }

    public Formatter tell(MessageChannel... messageChannels) {
        Text message = build();
        for (MessageChannel channel : messageChannels) {
            channel.send(message);
        }
        return this;
    }

    public Formatter tell(ChatType chatType, Player... receivers) {
        Text message = build();
        for (Player receiver : receivers) {
            receiver.sendMessage(chatType, message);
        }
        return this;
    }

    public Formatter tell(ChatType chatType, MessageChannel... messageChannels) {
        Text message = build();
        for (MessageChannel channel : messageChannels) {
            channel.send(message, chatType);
        }
        return this;
    }

    public Formatter tellPermitted(String permission) {
        return tell(MessageChannel.permission(permission));
    }

    public Formatter title(Player... receivers) {
        return title(null, null, null, receivers);
    }

    public Formatter title(int fade, Player... receivers) {
        return title(fade, null, fade, receivers);
    }

    public Formatter title(int fade, int stay, Player... receivers) {
        return title(fade, stay, fade, receivers);
    }

    public Formatter title(@Nullable Integer fadeIn, @Nullable Integer stay, @Nullable Integer fadeOut, Player... receivers) {
        Title title = title(fadeIn, stay, fadeOut);
        for (Player player : receivers) {
            player.sendTitle(title);
        }
        return this;
    }

    public Formatter log() {
        return tell(Sponge.getServer().getConsole());
    }

    @Override
    public Text toText() {
        return builder.build();
    }

    public HoverAction<Text> toHoverAction() {
        return TextActions.showText(toText());
    }
}
