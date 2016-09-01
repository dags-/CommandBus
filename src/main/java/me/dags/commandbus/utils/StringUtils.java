package me.dags.commandbus.utils;

/**
 * @author dags <dags@dags.me>
 */
public class StringUtils {

    public static String format(String in, Object... args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, a = 0, length = in.length(); i < length; i++) {
            char c = in.charAt(i);
            if (c == '{' && i + 1 < length && in.charAt(i + 1) == '}' && a < args.length) {
                i++;
                sb.append(args[a++]);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
