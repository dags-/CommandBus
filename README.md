# CommandBus
Another command annotation processing thing

[![Release](https://jitpack.io/v/dags-/CommandBus.svg)](https://jitpack.io/dags-/CommandBus)


#### example command code
```java
public class ExampleCommands {

    @Command(alias = "pm")
    @Description("Private message another player")
    public void message(@Caller CommandSource from, @One("to") Player to, @Join("message") String message) {
        Format.DEFAULT.stress("You => {}: ", to.getName()).info(message).tell(from);
        Format.DEFAULT.stress("{} => You: ", from.getName()).info(message).tell(to);
    }

    @Command(alias = "all", parent = "pm")
    @Assignment(role = "admin", permit = true)
    @Description("Send a private message to all matching players")
    @Permission(value = "exampleplugin.pm.all", description = "Allow use of the /pm all command")
    public void messageAll(@Caller CommandSource from, @All("to") Collection<Player> to, @Join("message") String message) {
        Format.DEFAULT.stress("You => All: ").info(message).tell(from);
        Format.DEFAULT.stress("{} => You: ", from.getName()).info(message).tell(to);
    }
}
```

#### example registration code
```java
@Plugin(..)
public class ExamplePlugin {

    public void ontInit() {
        CommandBus.create().register(ExampleCommands.class).register(new SomeOtherCommand()).submit(this);
    }
}
```
