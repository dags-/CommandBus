# CommandBus
Another command annotation processing thing

[![Release](https://jitpack.io/v/dags-/CommandBus.svg)](https://jitpack.io/#dags-/CommandBus)

### Features:
- Write commands as Java methods
- Easy registration - by class, object, or even by package
- Optional command flags
- Generates command usage and help texts
- Automatically generates & registers permissions
- Generates per-plugin markdown tables of all commands/permissions/descriptions

### Example Code:
```java
@Plugin(id = "example", name = "Example", version = "1.0", description = "Example plugin")
public class ExamplePlugin {

    @Listener
    public void init(GameInitializationEvent event) {
        CommandBus.create().register(this).submit();
    }

    /**
     * Sends a 'hello world' message to the command source
     */
    @Command("hello world")
    public void example0(@Src Player src) {
        Fmt.stress("hello world!").tell(src);
    }

    /**
     * Sends the <message> to the given <player>
     */
    @Permission
    @Description("Teleport the player to the world")
    @Command("example tp <player> <world>")
    public void example1(@Src CommandSource src, Player target, World world) {
        Fmt.info("Teleported ").stress(target).info(" to world ").stress(world).tell(src);
        Fmt.stress(src.getName()).info(" teleported you to ").stress(world).tell(target);
        target.setLocation(world.getSpawnLocation());
    }

    /**
     * Sends the <message> to all players matching <match>
     */
    @Permission
    @Description("Send a private message")
    @Command("example pma <match> <message>")
    public void example2(@Src Player src, Collection<Player> targets, @Join String message) {
        Fmt.stress("You -> All: ").info(message).tell(src);
        Fmt.stress("%s -> You: ", src.getName()).info(message).tell(targets);
    }

    /**
     * Prints the value of the optional 'bool' flag
     */
    @Flag("bool")
    @Permission
    @Description("An example command that accepts flags")
    @Command("example flags")
    public void example3(@Src Player src, Flags flags) {
        boolean bool = flags.getOr("bool", false);
        Fmt.info("Bool: ").stress(bool).tell(src);
    }

    /**
     * Prints the list of BlockTypes back to the user
     */
    @Permission
    @Description("An example command that accepts varargs")
    @Command("example varargs <block>")
    public void example4(@Src CommandSource src, BlockType... blocks) {
        for (int i = 0; i < blocks.length; i++) {
            Fmt.info("Block #%s: ", i).stress(blocks[i].getName()).tell(src);
        }
    }
}
```
