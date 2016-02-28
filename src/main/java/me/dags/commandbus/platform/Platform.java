package me.dags.commandbus.platform;

/**
 * @author dags <dags@dags.me>
 */

public enum Platform
{
    BUKKIT(""/*"me.dags.commandbus.platform.bukkit.BukkitPlatform"*/),
    SPONGE("me.dags.commandbus.platform.sponge.SpongeRegistrar"),
    NONE(""),
    ;

    private final String clazz;

    Platform(String clazz)
    {
        this.clazz = clazz;
    }

    private Class<?> getClazz() throws ClassNotFoundException
    {
        if (this == NONE || clazz.isEmpty())
        {
            return null;
        }
        return Class.forName(clazz);
    }

    public Registrar getPlatformRegistrar()
    {
        try
        {
            Class<?> c = getClazz();
            if (c != null)
            {
                return Registrar.class.cast(c.newInstance());
            }
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return (object, commandContainer, commandBus, list) -> {};
    }
}
