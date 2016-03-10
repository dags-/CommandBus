# CommandBus
Another command annotation processing thing

#### example command code
```java
public class ExampleCommands {

    @Command(aliases = "pm", perm = "exampleplugin.pm.single", desc = "Send a private message to someone")
    public void message(@Caller CommandSource from, Player to, @Join String message) {
        from.sendMessage(Text.of("You -> " + to.getName() + ": " + message));
        to.sendMessage(Text.of("" + from.getName() + " -> You: " + message));
    }

    @Command(aliases = "pma", perm = "exampleplugin.pm.all", desc = "Send a private message to all those whose name starts with <to>")
    public void messageAll(@Caller CommandSource from, @All Collection<Player> to, @Join String message) {
        from.sendMessage(Text.of("You -> ToAll: " + message));
        to.stream().filter(p -> p != from).forEach(p -> p.sendMessage(Text.of("" + from.getName() + " -> You: " + message)));
    }
}
```

#### example registration code
```java
@Plugin(..)
public class ExamplePlugin {

    public void ontInit() {
        CommandBus.newInstance().register(ExampleCommands.class).register(new SomeOtherCommand()).submit(this);
    }
}
```
