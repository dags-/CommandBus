# CommandBus
Another command annotation processing thing

# CommandBus
Another command annotation processing thing

### format
```
/command subCommand flag1:345 flag2:false flag3:someString flag4:'some String with spaces'
```
Where the ```/command subCommand``` directs to the desired Method (annotated with the @Command annotation)

Any flags following the above portion can be queried/accessed via the CommandEvent..flags() method


### code
```
class SomeClass
{
    @Command(command = "command subCommand1")
    public void exampleCommand1(CommandEvent<?> event)
    {
        event.flags().ifPresent("flag1", f -> System.out.println(f.number()));
        event.flags().ifPresent("flag2", f -> System.out.println(f.bool()));
        ...etc
    }

    @Command(command = "command subCommand2")
    @FlagFilter(require = {"flag3", "flag4"}, block = {"flag1", "flag2"})
    public void exampleCommand2(CommandEvent<?> event)
    {
        System.out.println(event.flags().get("flag3").string());
        System.out.println(event.flags().get("flag4").string());
        ...etc
    }
}


class CommandHandler
{
    private final CommandBus commandBus = new CommandBus().register(SomeClass.class);

    public void onCommandCalled(Object caller, String commandInput)
    {
        Result result = commandBus.call(caller, commandInput);
        result.onFail(r -> System.out.println(r.message));
    }
}
