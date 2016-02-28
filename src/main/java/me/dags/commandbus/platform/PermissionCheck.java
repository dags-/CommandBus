package me.dags.commandbus.platform;

/**
 * @author dags <dags@dags.me>
 */

public interface PermissionCheck
{
    boolean hasPermission(Object source, String permission);
}
