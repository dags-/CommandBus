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

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author dags <dags@dags.me>
 */
public final class FMT {

    private static final Format FORMAT;

    private FMT(){}

    public static FormattedListBuilder listBuilder() {
        return FORMAT.listBuilder();
    }

    public static Formatter fmt() {
        return FORMAT.message();
    }

    public static Formatter info(Object input, Object... args) {
        return FORMAT.info(input, args);
    }

    public static Formatter stress(Object input, Object... args) {
        return FORMAT.stress(input, args);
    }

    public static Formatter subdued(Object input, Object... args) {
        return FORMAT.subdued(input, args);
    }

    public static Formatter error(Object input, Object... args) {
        return FORMAT.error(input, args);
    }

    public static Formatter warn(Object input, Object... args) {

        Sponge.getServer().isMainThread();
        return FORMAT.warn(input, args);
    }

    public static Formatter text(Text text) {
        return FORMAT.text(text);
    }

    public static Format copy() {
        return FORMAT.copy();
    }

    public static Format.Builder builder() {
        return FORMAT.toBuilder();
    }

    private static Format load(Path path) throws IOException {
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(path).build();
        if (Files.exists(path)) {
            CommentedConfigurationNode node = loader.load();
            return FormatSerializer.deserialize(node);
        } else {
            FMT.save(path, Format.DEFAULT);
            return Format.DEFAULT;
        }
    }

    static {
        Format fmt;

        try {
            Path dir = getConfigDir();
            Path config = dir.resolve("global_formatter.conf");
            fmt = load(config);
        } catch (IOException e) {
            fmt = Format.DEFAULT;
        }

        FORMAT = fmt != null ? fmt : Format.DEFAULT;
    }

    private static void save(Path path, Format format) throws IOException {
        Files.createDirectories(path.getParent());
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(path).build();
        CommentedConfigurationNode node = loader.createEmptyNode();
        FormatSerializer.serialize(format, node);
        loader.save(node);
    }

    private static Path getConfigDir() throws IOException {
        Path configDir = Sponge.getGame().getGameDirectory().resolve("config").resolve("commandbus");

        if (Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }

        return configDir;
    }
}
