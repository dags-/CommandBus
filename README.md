# CommandBus
Another command annotation processing thing

[![Release](https://jitpack.io/v/dags-/CommandBus.svg)](https://jitpack.io/#dags-/CommandBus)


#### example command code
```java
public class ExampleCommands {

    @Command(alias = "pm")
    public void message(@Caller CommandSource from, @One("to") Player to, @Join("message") String message) {
        FMT.stress("You => %s: ", to.getName()).info(message).tell(from);
        FMT.stress("%s => You: ", from.getName()).info(message).tell(to);
    }

    @Command(alias = "all", parent = "pm")
    @Assignment(role = "admin", permit = true)
    @Description("Send a private message to all matching players")
    @Permission(value = "exampleplugin.pm.all", description = "Allow use of the '/pm all' command")
    public void messageAll(@Caller CommandSource from, @All("to") Collection<Player> to, @Join("message") String message) {
        FMT.stress("You => All: ").info(message).tell(from);
        FMT.stress("%s => You: ", from.getName()).info(message).tell(to);
    }
}
```

#### example registration code
```java
@Plugin(..)
public class ExamplePlugin {

    public void ontInit(GameInitializationEvent event) {
        // Register commands in a specific class
        CommandBus.create().register(ExampleCommands.class).submit(this);
        
        // Register commands in a specific object
        CommandBus.create().register(new ExampleCommands(..)).submit(this);
        
        // Register commands from all classes in the same package as ExampleCommands.class
        CommandBus.create().registerPackageOf(ExampleCommands.class).submit(this);
        
        // Register commands from all classes in the same package as ExampleCommands.class and all it's sub-packages
        CommandBus.create().registerSubPackagesOf(ExampleCommands.class).submit(this);
    }
}
```
