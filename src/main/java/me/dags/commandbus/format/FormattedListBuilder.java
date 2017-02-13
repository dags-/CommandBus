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

import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class FormattedListBuilder {

    private final Format format;
    private final List<Text> lines = new ArrayList<>();
    private Formatter lineFormatter;
    private Formatter titleFormatter;
    private Formatter headerFormatter;
    private Formatter footerFormatter;
    private Formatter paddingFormatter;
    private int linesPerPage = 9;

    FormattedListBuilder(Format format) {
        this.format = format;
        this.lineFormatter = format.message();
        this.titleFormatter = format.message();
        this.headerFormatter = format.message();
        this.footerFormatter = format.message();
        this.paddingFormatter = format.message();
    }

    public FormattedListBuilder linesPerPage(int lines) {
        this.linesPerPage = lines;
        return this;
    }

    public Formatter line() {
        return flushLine().lineFormatter = format.message();
    }

    public Formatter title() {
        return titleFormatter = format.message();
    }

    public Formatter header() {
        return headerFormatter = format.message();
    }

    public Formatter footer() {
        return footerFormatter = format.message();
    }

    public Formatter padding() {
        return paddingFormatter = format.message();
    }

    public PaginationList build() {
        flushLine();
        PaginationList.Builder builder = PaginationList.builder();
        builder.contents(lines);
        builder.linesPerPage(linesPerPage);
        applyNonEmpty(titleFormatter, builder::title);
        applyNonEmpty(headerFormatter, builder::header);
        applyNonEmpty(footerFormatter, builder::footer);
        applyNonEmpty(paddingFormatter, builder::padding);
        return builder.build();
    }

    private FormattedListBuilder flushLine() {
        if (!lineFormatter.isEmpty()) {
            lines.add(lineFormatter.build());
        }
        return this;
    }

    private void applyNonEmpty(Formatter formatter, Consumer<Text> consumer) {
        if (!formatter.isEmpty()) {
            consumer.accept(formatter.build());
        }
    }
}
