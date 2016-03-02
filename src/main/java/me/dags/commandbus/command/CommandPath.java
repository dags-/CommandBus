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

/**
 * @author dags <dags@dags.me>
 */

public class CommandPath
{
    private final String[] path;

    public CommandPath(String in)
    {
        this.path = in.toLowerCase().split(" ");
    }

    public String at(int depth)
    {
        return depth < path.length ? path[depth] : "";
    }

    public String to(int depth)
    {
        StringBuilder sb = new StringBuilder(path[0]);
        for (int i = 1; i <= depth && i < path.length; i++)
            sb.append(" ").append(path[i]);
        return sb.toString();
    }

    public String from(int depth)
    {
        StringBuilder sb = new StringBuilder(path[depth]);
        for (int i = depth; i < maxDepth() && i < path.length; i++)
            sb.append(" ").append(path[i]);
        return sb.toString();
    }

    public int maxDepth()
    {
        return path.length - 1;
    }
}