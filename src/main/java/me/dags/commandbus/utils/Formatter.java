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

package me.dags.commandbus.utils;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;

/**
 * @author dags <dags@dags.me>
 */
public class Formatter {

    private TextFormat text = TextFormat.of(TextColors.DARK_AQUA);
    private TextFormat subdued = TextFormat.of(TextColors.GRAY);
    private TextFormat highlight = TextFormat.of(TextColors.DARK_PURPLE);
    private TextFormat error = TextFormat.of(TextColors.GRAY);
    private TextFormat warn = TextFormat.of(TextColors.RED);

    private Text of(String message, TextFormat format) {
        return Text.builder(message).format(format).build();
    }

    public Formatter textFormat(TextFormat format) {
        this.text = format;
        return this;
    }

    public Formatter subduedFormat(TextFormat format) {
        this.subdued = format;
        return this;
    }

    public Formatter stressFormat(TextFormat format) {
        this.highlight = format;
        return this;
    }

    public Formatter errorFormat(TextFormat format) {
        this.error = format;
        return this;
    }

    public Formatter warnFormat(TextFormat format) {
        this.warn = format;
        return this;
    }

    public Builder info(Object input) {
        return builder().info(input);
    }

    public Builder stress(Object input) {
        return builder().stress(input);
    }

    public Builder error(Object input) {
        return builder().error(input);
    }

    public Builder warn(Object input) {
        return builder().warn(input);
    }

    public Builder text(Text text) {
        return builder().append(text);
    }

    private Text infoText(String message) {
        return of(message, text);
    }

    private Text stressText(String message) {
        return of(message, highlight);
    }

    private Text errorText(String message) {
        return of(message, error);
    }

    private Text warnText(String message) {
        return of(message, warn);
    }

    public Builder builder() {
        return new Builder();
    }

    public class Builder {

        private final Text.Builder builder = Text.builder();

        public Builder info(Object input) {
            builder.append(Formatter.this.infoText(input.toString()));
            return this;
        }

        public Builder stress(Object input) {
            builder.append(Formatter.this.stressText(input.toString()));
            return this;
        }

        public Builder error(Object input) {
            builder.append(Formatter.this.errorText(input.toString()));
            return this;
        }

        public Builder warn(Object input) {
            builder.append(Formatter.this.warnText(input.toString()));
            return this;
        }

        public Builder append(Text text) {
            builder.append(text);
            return this;
        }

        public Text build() {
            return builder.build();
        }

        public Builder tell(MessageReceiver receiver) {
            receiver.sendMessage(build());
            return this;
        }

        public Builder tell(MessageChannel messageChannel) {
            messageChannel.send(build());
            return this;
        }

        public Builder tell(MessageReceiver... receivers) {
            for (MessageReceiver receiver : receivers) {
                tell(receiver);
            }
            return this;
        }
    }
}
