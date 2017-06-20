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
        // Can register multiple objects/classes/packages before executing submit()
        CommandBus.create(this).register(this).submit();
    }

    /**
     * /hello world
     * 
     * @param src The source of the command - Note! We must explicitly state that the Player is the command source
     *                                        using @Src as it would otherwise be interpretted as a parameter
     */
    @Command(alias = "world", parent = "hello")
    public void example0(@Src Player src) {
        Fmt.stress("hello world!").tell(src);
    }

    /**
     * /example pm <player> <message>
     *     
     * @param src The source of the command (inferred) - Note! Because src is specified as a CommandSource, the @Src
     *                                                   annotation is not required
     * @param target Accepts an online player's name
     * @param message The remaining arguments joined by space characters
     */
    @Permission
    @Command(alias = "pm", parent = "example")
    @Description("Send a private message")
    public void example1(CommandSource src, Player target, @Join String message) {
        Fmt.stress("You -> %s: ", target.getName()).info(message).tell(src);
        Fmt.stress("%s -> You: ", src.getName()).info(message).tell(src);
    }

    /**
     * /example pma <player> <message>
     * 
     * @param src The source of the command (explicit)
     * @param targets Collects all online players whose name matches the input
     * @param message The remaining arguments joined by space characters
     */
    @Permission
    @Command(alias = "pma", parent = "example")
    @Description("Send a private message")
    public void example2(@Src Player src, Collection<Player> targets, @Join String message) {
        Fmt.stress("You -> All: ").info(message).tell(src);
        Fmt.stress("%s -> You: ", src.getName()).info(message).tell(targets);
    }

    /**
     * /example flags (-bool | --int <integer> | --user <user>)
     * 
     * @param src The source of the command (explicit)
     * @param flags A MultiHashMap containing any flags that may have been parsed
     */
    @Permission
    @Command(alias = "flags", parent = "example")
    @Description("An example command that accepts flags")
    @Flags({@Flag("bool"), @Flag(value = "int", type = int.class), @Flag(value = "user", type = User.class)})
    public void example3(@Src Player src, CommandFlags flags) {
        Fmt.info("Bool: ").stress(flags.getOrDefault("bool", false))
                .line().info("Int: ").stress(flags.getOrDefault("int", 0))
                .line().info("User: ").stress(flags.getOrDefault("user", src))
                .tell(src);
    }

    /**
     * /example varargs <blocktype...>
     * 
     * @param src The source of the command (inferred)
     * @param blocks A variable length array of BlockTypes parsed from the src's input
     */
    @Permission
    @Command(alias = "varargs", parent = "example")
    @Description("An example command that accepts varargs")
    public void example4(CommandSource src, BlockType... blocks) {
        for (int i = 0; i < blocks.length; i++) {
            Fmt.info("Block #%s: ", i).stress(blocks[i].getName()).tell(src);
        }
    }
}
```
