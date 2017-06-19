package me.dags.commandbus.utils;

import me.dags.commandbus.command.CommandMethod;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public class TableWriter implements Closeable {

    private final Appendable appendable;

    public TableWriter(Appendable appendable) {
        this.appendable = appendable;
    }

    public void writeHeaders() {
        write("| Command | Permission | Description |").newLine();
        write("| :------ | :--------- | :---------- |");
    }

    public void writeMethod(CommandMethod method) {
        String command = method.commandString();
        String permission = method.permission().value();
        String description = method.description().value();
        newLine().write(String.format("| `%s` | `%s` | %s |", command, permission, description));
    }

    private TableWriter write(String string) {
        try {
            appendable.append(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    private TableWriter newLine() {
        write("\n");
        return this;
    }

    @Override
    public void close() throws IOException {
        if (appendable instanceof Flushable) {
            Flushable flushable = (Flushable) appendable;
            flushable.flush();
        }
        if (appendable instanceof Closeable) {
            Closeable closeable = (Closeable) appendable;
            closeable.close();
        }
    }
}
