# CommandBus
Another command annotation processing thing

#### example command code
```java
public class ExampleCommands {

    @Command(aliases = "pm", perm = "ExamplePlugin.pm.single", desc = "Send a private message to someone")
    public void message(@Caller CommandSource from, @One("to") Player to, @Join("message") String message) {
        from.sendMessage(Text.of("You -> " + to.getName() + ": " + message));
        to.sendMessage(Text.of("" + from.getName() + " -> You: " + message));
    }

    @Command(aliases = "pma", perm = "ExamplePlugin.pm.all", desc = "Send a private to all those whose name starts with <to>")
    public void messageAll(@Caller CommandSource from, @All("to") Collection<Player> to, @Join("message") String message) {
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
        CommandBus bus = new CommandBus();
        bus.register(ExampleCommands.class);
        bus.register(new SomeOtherCommand());
        bus.submit(this);
    }
}
```