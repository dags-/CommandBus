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

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.*;

import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class Format {

    public static final TypeToken<Format> TYPE_TOKEN = TypeToken.of(Format.class);
    static final Format DEFAULT = new Builder().build();

    final TextFormat info;
    final TextFormat subdued;
    final TextFormat stress;
    final TextFormat error;
    final TextFormat warn;

    private Format(Builder builder) {
        this.info = builder.info;
        this.subdued = builder.subdued;
        this.stress = builder.stress;
        this.error = builder.error;
        this.warn = builder. warn;
    }

    public Format setNode(ConfigurationNode node) throws ObjectMappingException {
        node.setValue(TYPE_TOKEN, this);
        return this;
    }

    public FormattedListBuilder listBuilder() {
        return new FormattedListBuilder(this);
    }

    public Formatter info(Object input, Object... args) {
        return message().info(input, args);
    }

    public Formatter subdued(Object input, Object... args) {
        return message().subdued(input, args);
    }

    public Formatter stress(Object input, Object... args) {
        return message().stress(input, args);
    }

    public Formatter error(Object input, Object... args) {
        return message().error(input, args);
    }

    public Formatter warn(Object input, Object... args) {
        return message().warn(input, args);
    }

    public Formatter text(Text text) {
        return message().append(text);
    }

    private Text of(String message, TextFormat format) {
        return Text.builder(message).format(format).build();
    }

    Text infoText(String message) {
        return of(message, info);
    }

    Text subduedText(String message) {
        return of(message, subdued);
    }

    Text stressText(String message) {
        return of(message, stress);
    }

    Text errorText(String message) {
        return of(message, error);
    }

    Text warnText(String message) {
        return of(message, warn);
    }

    public Map<Object, Object> toMap() {
        return FormatSerializer.toMap(this);
    }

    public Format copy() {
        return toBuilder().build();
    }

    public Format.Builder toBuilder() {
        return new Builder().info(info).stress(stress).subdued(subdued).error(error).warn(warn);
    }


    public Formatter message() {
        return new Formatter(this);
    }

    public static Format fromNode(ConfigurationNode node) throws ObjectMappingException {
        if (!node.hasMapChildren()) {
            return new Builder().build().setNode(node);
        }
        return node.getValue(Format.TYPE_TOKEN);
    }

    public static Format fromMap(Map<Object, Object> map) {
        return FormatSerializer.fromMap(map);
    }

    public static Format.Builder builder() {
        return new Builder();
    }

    public static class Builder {

        TextFormat info = TextFormat.of(TextColors.WHITE);
        TextFormat subdued = TextFormat.of(TextStyles.ITALIC);
        TextFormat stress = TextFormat.of(TextColors.GREEN);
        TextFormat error = TextFormat.of(TextColors.GRAY);
        TextFormat warn = TextFormat.of(TextColors.RED);

        public Builder info(TextFormat format) {
            this.info = format;
            return this;
        }

        public Builder info(TextStyle... style) {
            return info(TextFormat.of(TextStyles.of(style)));
        }

        public Builder info(TextColor color, TextStyle... style) {
            return info(TextFormat.of(color, TextStyles.of(style)));
        }

        public Builder subdued(TextFormat format) {
            this.subdued = format;
            return this;
        }

        public Builder subdued(TextStyle... style) {
            return subdued(TextFormat.of(TextStyles.of(style)));
        }

        public Builder subdued(TextColor color, TextStyle... style) {
            return subdued(TextFormat.of(color, TextStyles.of(style)));
        }

        public Builder stress(TextFormat format) {
            this.stress = format;
            return this;
        }

        public Builder stress(TextStyle... style) {
            return stress(TextFormat.of(TextStyles.of(style)));
        }

        public Builder stress(TextColor color, TextStyle... style) {
            return stress(TextFormat.of(color, TextStyles.of(style)));
        }

        public Builder error(TextFormat format) {
            this.error = format;
            return this;
        }

        public Builder error(TextStyle... style) {
            return error(TextFormat.of(TextStyles.of(style)));
        }

        public Builder error(TextColor color, TextStyle... style) {
            return error(TextFormat.of(color, TextStyles.of(style)));
        }

        public Builder warn(TextFormat format) {
            this.warn = format;
            return this;
        }

        public Builder warn(TextStyle... style) {
            return warn(TextFormat.of(TextStyles.of(style)));
        }

        public Builder warn(TextColor color, TextStyle... style) {
            return warn(TextFormat.of(color, TextStyles.of(style)));
        }

        public Format build() {
            return new Format(this);
        }
    }
}
