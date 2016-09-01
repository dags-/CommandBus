package me.dags.commandbus.utils;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.format.*;

import java.util.LinkedHashMap;
import java.util.Map;

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
