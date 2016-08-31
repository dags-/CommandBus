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

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class Format {

    public static final TypeToken<Format> TYPE_TOKEN = TypeToken.of(Format.class);
    public static final Format.Adapter TYPE_ADAPTER = new Format.Adapter();
    static final Format DEFAULT = Format.builder().build();

    private static final String INFO = "info";
    private static final String SUBDUED = "subdued";
    private static final String STRESS = "stress";
    private static final String ERROR = "error";
    private static final String WARN = "warn";

    private final TextFormat info;
    private final TextFormat subdued;
    private final TextFormat stress;
    private final TextFormat error;
    private final TextFormat warn;

    private Format(Builder builder) {
        this.info = builder.info;
        this.subdued = builder.subdued;
        this.stress = builder.stress;
        this.error = builder.error;
        this.warn = builder. warn;
    }

    public Format setNode(String key, ConfigurationNode parent) throws ObjectMappingException {
        parent.getNode(key).setValue(TYPE_TOKEN, this);
        return this;
    }

    public Format setNode(ConfigurationNode node) throws ObjectMappingException {
        node.setValue(TYPE_TOKEN, this);
        return this;
    }

    public MessageBuilder info(Object input) {
        return message().info(input);
    }

    public MessageBuilder subdued(Object input) {
        return message().subdued(input);
    }

    public MessageBuilder stress(Object input) {
        return message().stress(input);
    }

    public MessageBuilder error(Object input) {
        return message().error(input);
    }

    public MessageBuilder warn(Object input) {
        return message().warn(input);
    }

    public MessageBuilder text(Text text) {
        return message().append(text);
    }

    private Text of(String message, TextFormat format) {
        return Text.builder(message).format(format).build();
    }

    private Text infoText(String message) {
        return of(message, info);
    }

    private Text subduedText(String message) {
        return of(message, subdued);
    }

    private Text stressText(String message) {
        return of(message, stress);
    }

    private Text errorText(String message) {
        return of(message, error);
    }

    private Text warnText(String message) {
        return of(message, warn);
    }

    public Map<Object, Object> toMap() {
        return Format.toMap(this);
    }

    public MessageBuilder message() {
        return new MessageBuilder();
    }

    public static Builder builder() {
        return new Builder();
    }

    public class MessageBuilder {

        private final Text.Builder builder = Text.builder();

        public MessageBuilder info(Object input) {
            builder.append(Format.this.infoText(input.toString()));
            return this;
        }

        public MessageBuilder subdued(Object input) {
            builder.append(Format.this.subduedText(input.toString()));
            return this;
        }

        public MessageBuilder stress(Object input) {
            builder.append(Format.this.stressText(input.toString()));
            return this;
        }

        public MessageBuilder error(Object input) {
            builder.append(Format.this.errorText(input.toString()));
            return this;
        }

        public MessageBuilder warn(Object input) {
            builder.append(Format.this.warnText(input.toString()));
            return this;
        }

        public MessageBuilder append(Text text) {
            builder.append(text);
            return this;
        }

        public Text build() {
            return builder.build();
        }

        public MessageBuilder tell(MessageReceiver receiver) {
            receiver.sendMessage(build());
            return this;
        }

        public MessageBuilder tell(MessageChannel messageChannel) {
            messageChannel.send(build());
            return this;
        }

        public MessageBuilder tell(MessageReceiver... receivers) {
            for (MessageReceiver receiver : receivers) {
                tell(receiver);
            }
            return this;
        }
    }

    public static class Builder {

        TextFormat info = TextFormat.of(TextColors.DARK_AQUA);
        TextFormat subdued = TextFormat.of(TextColors.GRAY);
        TextFormat stress = TextFormat.of(TextColors.DARK_PURPLE);
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

    public static class Adapter implements TypeSerializer<Format> {

        private Adapter(){}

        @Override
        public Format deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            Format.Builder formatter = Format.builder();
            formatter.info(getFormat(value.getNode(INFO)));
            formatter.subdued(getFormat(value.getNode(SUBDUED)));
            formatter.stress(getFormat(value.getNode(STRESS)));
            formatter.error(getFormat(value.getNode(ERROR)));
            formatter.warn(getFormat(value.getNode(WARN)));
            return formatter.build();
        }

        @Override
        public void serialize(TypeToken<?> type, Format format, ConfigurationNode value) throws ObjectMappingException {
            setFormat(format.info, value.getNode(INFO));
            setFormat(format.subdued, value.getNode(SUBDUED));
            setFormat(format.stress, value.getNode(STRESS));
            setFormat(format.error, value.getNode(ERROR));
            setFormat(format.warn, value.getNode(WARN));
        }

        private static TextFormat getFormat(ConfigurationNode node) {
            if (node.isVirtual()) {
                return TextFormat.NONE;
            }
            String color = node.getNode("color").getString("");
            boolean bold = node.getNode("bold").getBoolean(false);
            boolean italic = node.getNode("italic").getBoolean(false);
            boolean obfuscated = node.getNode("obfuscated").getBoolean(false);
            boolean strikethrough = node.getNode("strikethrough").getBoolean(false);
            boolean underline = node.getNode("underline").getBoolean(false);
            TextColor textColor = Sponge.getRegistry().getType(TextColor.class, color).orElse(TextColors.NONE);
            TextStyle style = TextStyles.of().bold(bold).italic(italic).obfuscated(obfuscated).strikethrough(strikethrough).underline(underline);
            return TextFormat.of(textColor, style);
        }

        private static void setFormat(TextFormat format, ConfigurationNode node) {
            if (format.getColor() != TextColors.NONE) {
                node.getNode("color").setValue(format.getColor().getName());
            }
            System.out.println(format.getStyle().isBold());
            System.out.println(format.getStyle().isItalic());
            System.out.println(format.getStyle().hasStrikethrough());
            System.out.println(format.getStyle().hasUnderline());

            format.getStyle().isBold().ifPresent(node.getNode("bold")::setValue);
            format.getStyle().isItalic().ifPresent(node.getNode("italic")::setValue);
            format.getStyle().isObfuscated().ifPresent(node.getNode("obfuscated")::setValue);
            format.getStyle().hasStrikethrough().ifPresent(node.getNode("strikethrough")::setValue);
            format.getStyle().hasUnderline().ifPresent(node.getNode("underline")::setValue);
        }
    }

    public static Map<Object, Object> toMap(Format format) {
        Map<Object, Object> map = new LinkedHashMap<>();
        map.put(INFO, toMap(format.info));
        map.put(SUBDUED, toMap(format.subdued));
        map.put(STRESS, toMap(format.stress));
        map.put(ERROR, toMap(format.error));
        map.put(WARN, toMap(format.warn));
        return map;
    }

    public static Format fromMap(Map<Object, Object> map) {
        Format.Builder formatter = Format.builder();
        formatter.info(fromObject(map.get(INFO)));
        formatter.subdued(fromObject(map.get(SUBDUED)));
        formatter.stress(fromObject(map.get(STRESS)));
        formatter.error(fromObject(map.get(ERROR)));
        formatter.warn(fromObject(map.get(WARN)));
        return formatter.build();
    }

    private static Map<Object, Object> toMap(TextFormat format) {
        Map<Object, Object> map = new LinkedHashMap<>();
        if (format.getColor() != TextColors.NONE) {
            map.put("color", format.getColor());
        }
        format.getStyle().isBold().ifPresent(b -> map.put("bold", b));
        format.getStyle().isItalic().ifPresent(b -> map.put("italic", b));
        format.getStyle().isObfuscated().ifPresent(b -> map.put("bold", b));
        format.getStyle().hasStrikethrough().ifPresent(b -> map.put("strikethrough", b));
        format.getStyle().hasUnderline().ifPresent(b -> map.put("underline", b));
        return map;
    }

    private static TextFormat fromObject(Object object) {
        if (object == null || !(object instanceof Map)) {
            return TextFormat.NONE;
        }
        Map map = (Map) object;
        String color = notNull((String) map.get("color"), "");
        Boolean bold = notNull((Boolean) map.get("bold"), false);
        Boolean italic = notNull((Boolean) map.get("italic"), false);
        Boolean obfuscated = notNull((Boolean) map.get("obfuscated"), false);
        Boolean strikethrough = notNull((Boolean) map.get("strikethrough"), false);
        Boolean underline = notNull((Boolean) map.get("underline"), false);
        TextColor textColor = Sponge.getRegistry().getType(TextColor.class, color).orElse(TextColors.NONE);
        TextStyle style = TextStyles.of().bold(bold).italic(italic).obfuscated(obfuscated).strikethrough(strikethrough).underline(underline);
        return TextFormat.of(textColor, style);
    }

    private static <T> T notNull(T in, T def) {
        return in != null ? in : def;
    }
}
