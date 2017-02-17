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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import me.dags.commandbus.format.FMT;
import me.dags.commandbus.format.Format;
import org.spongepowered.api.command.CommandSource;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */

/**
 * Cache for assigning Objects to CommandSources that expire after a certain duration, and alert the source on expirey.
 * Allows 'Builder pattern' style commands that are associated only to the Caller of the command.
 * Weakly references the CommandSource key.
 *
 * @param <K> Key type - must extend CommandSource
 * @param <V> Value type - any Object
 */
public class CommandSourceCache<K extends CommandSource, V> implements RemovalListener<K, V> {

    private final long expireTime;
    private final TimeUnit timeUnit;
    private final Cache<K, V> cache;
    private final Format messageFormat;
    private final String addMessage;
    private final String expireMessage;
    private final String noElementMessage;

    private CommandSourceCache(Builder<? super K, ? super V> builder) {
        this.expireTime = builder.expireTime;
        this.timeUnit = builder.timeUnit;
        this.messageFormat = builder.messageFormat;
        this.addMessage = builder.addMessage;
        this.expireMessage = builder.expireMessage;
        this.noElementMessage = builder.noElementMessage;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(builder.expireTime, builder.timeUnit)
                .removalListener(this)
                .weakKeys()
                .build();
    }

    /**
     * Get the permit for the given CommandSource.
     * Alerts the CommandSource if absent.
     *
     * @param k The CommandSource
     * @return Optional of the permit - may be empty
     */
    public Optional<V> get(K k) {
        V v = cache.getIfPresent(k);
        if (v == null && !noElementMessage.isEmpty()) {
            messageFormat.error(noElementMessage).tell(k);
        }
        return Optional.ofNullable(v);
    }

    /**
     * Add a given CommandSource and Value pair to the cache.
     * Notify the CommandSource when the Value has been added.
     *
     * @param k The CommandSource
     * @param v The Value
     */
    public void add(K k, V v) {
        if (k != null) {
            cache.put(k, v);
            if (!addMessage.isEmpty()) {
                messageFormat.subdued(expireMessage).tell(k);
            }
        }
    }

    /**
     * Remove the entry for the given CommandSource
     *
     * @param k The CommandSource to remove
     */
    public void remove(K k) {
        cache.invalidate(k);
    }

    @Override
    public void onRemoval(RemovalNotification<K, V> notification) {
        CommandSource k = notification.getKey();
        if (k != null && !expireMessage.isEmpty()) {
            messageFormat.subdued(expireMessage).tell(k);
        }
    }

    public static <K extends CommandSource, V> Builder<K, V> builder() {
        return new Builder<K, V>(){};
    }

    public static class Builder<K extends CommandSource, V> {

        private long expireTime = 60L;
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        private Format messageFormat = FMT.copy();
        private String addMessage = "";
        private String expireMessage = "";
        private String noElementMessage = "";

        public Builder<K, V> expireTime(long expireTime) {
            this.expireTime = expireTime;
            return this;
        }

        public Builder<K, V> timeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public Builder<K, V> messageFormat(Format messageFormat) {
            this.messageFormat = messageFormat;
            return this;
        }

        public Builder<K, V> addMessage(String addMessage) {
            this.addMessage = addMessage;
            return this;
        }

        public Builder<K, V> expireMessage(String expireMessage) {
            this.expireMessage = expireMessage;
            return this;
        }

        public Builder<K, V> noElementMessage(String expireMessage) {
            this.expireMessage = expireMessage;
            return this;
        }

        public <K1 extends K, V1 extends V> CommandSourceCache<K1, V1> build() {
            return new CommandSourceCache<>(this);
        }
    }
}
