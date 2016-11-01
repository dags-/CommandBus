# CommandBus
Another command annotation processing thing

[![Build Status](https://travis-ci.org/dags-/CommandBus.svg?branch=master)](https://travis-ci.org/dags-/CommandBus)


#### example command code
```java
public class ExampleCommands {

    @Command(
            aliases = "pm",
            desc = "Private message someone",
            perm = @Permission(
                    value = "exampleplugin.pm.one",
                    assign = @Assignment(role = "user", value = true)
            )
    )
    public void message(@Caller CommandSource from, @One("to") Player to, @Join("message") String message) {
        Format.DEFAULT.stress("You => {}: ", to.getName()).info(message).tell(from);
        Format.DEFAULT.stress("{} => You: ", from.getName()).info(message).tell(to);
    }

    @Command(
            aliases = "pma",
            desc = "Private message everyone",
            perm = @Permission(
                    value = "exampleplugin.pm.all",
                    description = "Grants the User use of '/pma'",
                    assign = @Assignment(role = "admin", value = true)
            )
    )
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
