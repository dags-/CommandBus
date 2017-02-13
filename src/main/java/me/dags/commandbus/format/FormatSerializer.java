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
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.format.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
public class FormatSerializer implements TypeSerializer<Format> {

    public static FormatSerializer INSTANCE = new FormatSerializer();

    private static final String INFO = "info";
    private static final String SUBDUED = "subdued";
    private static final String STRESS = "stress";
    private static final String ERROR = "error";
    private static final String WARN = "warn";

    private FormatSerializer(){}

    @Override
    public Format deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        return deserialize(value);
    }

    @Override
    public void serialize(TypeToken<?> type, Format format, ConfigurationNode value) throws ObjectMappingException {
        serialize(format, value);
    }

    static Format deserialize(ConfigurationNode value) {
        Format.Builder builder = Format.builder();
        builder.info(getFormat(value.getNode(INFO)));
        builder.subdued(getFormat(value.getNode(SUBDUED)));
        builder.stress(getFormat(value.getNode(STRESS)));
        builder.error(getFormat(value.getNode(ERROR)));
        builder.warn(getFormat(value.getNode(WARN)));
        return builder.build();
    }

    static void serialize(Format format, ConfigurationNode value) {
        setFormat(format.info, value.getNode(INFO));
        setFormat(format.subdued, value.getNode(SUBDUED));
        setFormat(format.stress, value.getNode(STRESS));
        setFormat(format.error, value.getNode(ERROR));
        setFormat(format.warn, value.getNode(WARN));
    }

    static TextFormat getFormat(ConfigurationNode node) {
        if (node.isVirtual()) {
            return TextFormat.NONE;
        }

        String colorId = node.getNode("color").getString(null);
        TextColor color = Sponge.getRegistry().getType(TextColor.class, colorId).orElse(null);

        TextStyle style = TextStyles.of();
        style = applyNonNull(style, style::bold, (Boolean) node.getNode("bold").getValue((Object) null));
        style = applyNonNull(style, style::italic, (Boolean) node.getNode("italic").getValue((Object) null));
        style = applyNonNull(style, style::underline, (Boolean) node.getNode("underline").getValue((Object) null));
        style = applyNonNull(style, style::obfuscated, (Boolean) node.getNode("obfuscated").getValue((Object) null));
        style = applyNonNull(style, style::strikethrough, (Boolean) node.getNode("strikethrough").getValue((Object) null));

        TextFormat format = TextFormat.of(style);
        return applyNonNull(format, format::color, color);
    }

    static void setFormat(TextFormat format, ConfigurationNode node) {
        if (format.getColor() != TextColors.NONE) {
            node.getNode("color").setValue(format.getColor().getName());
        }

        format.getStyle().isBold().ifPresent(node.getNode("bold")::setValue);
        format.getStyle().isItalic().ifPresent(node.getNode("italic")::setValue);
        format.getStyle().isObfuscated().ifPresent(node.getNode("obfuscated")::setValue);
        format.getStyle().hasStrikethrough().ifPresent(node.getNode("strikethrough")::setValue);
        format.getStyle().hasUnderline().ifPresent(node.getNode("underline")::setValue);
    }

    static Map<Object, Object> toMap(Format format) {
        Map<Object, Object> map = new LinkedHashMap<>();
        map.put(INFO, toMap(format.info));
        map.put(SUBDUED, toMap(format.subdued));
        map.put(STRESS, toMap(format.stress));
        map.put(ERROR, toMap(format.error));
        map.put(WARN, toMap(format.warn));
        return map;
    }

    static Format fromMap(Map<Object, Object> map) {
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
            map.put("color", format.getColor().getId());
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
        String colorId = (String) map.get("color");
        colorId = colorId != null ? colorId : "";
        TextColor color = Sponge.getRegistry().getType(TextColor.class, colorId).orElse(null);

        TextStyle style = TextStyles.of();
        style = applyNonNull(style, style::bold, (Boolean) map.get("bold"));
        style = applyNonNull(style, style::italic, (Boolean) map.get("italic"));
        style = applyNonNull(style, style::obfuscated, (Boolean) map.get("obfuscated"));
        style = applyNonNull(style, style::strikethrough, (Boolean) map.get("strikethrough"));
        style = applyNonNull(style, style::underline, (Boolean) map.get("underline"));

        TextFormat format = TextFormat.of(style);
        return applyNonNull(format, format::color, color);
    }

    private static <T, V> T applyNonNull(T style, Function<V, T> func, V value) {
        if (value != null) {
            return func.apply(value);
        }
        return style;
    }
}
