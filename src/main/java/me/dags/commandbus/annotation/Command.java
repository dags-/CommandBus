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

package me.dags.commandbus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dags <dags@dags.me>
 */

/**
 * The @Command annotation is used to mark a method as a Command.
 * CommandBus will process the provided information from the @Command annotation
 * and the Method's parameters to generate an appropriate Command.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    /**
     * Provide either a single String alias for this Command, or an Array of alias.
     * The first alias provided will be used as the Command's 'main' alias.
     */
    String[] alias();

    /**
     * If this @Command represents a sub-command, the parent command(s) alias(es) should
     * be specified here.
     * <p>
     * Example:
     * For subcommand2 in: '/maincommand subcommand1 subcommand2', the parent would be
     * 'parent = "maincommand subcommand1"'.
     * <p>
     * If a given subcommand's parent(s) has/have not been registered with the CommandBus,
     * CommandBus will generate an empty 'Command stub' to represent it.
     */
    String parent() default "";

    Assignment assign() default @Assignment(role = "", permit = false);

    Permission permission() default @Permission("");

    Description description() default @Description("");
}
