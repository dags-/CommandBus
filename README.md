# CommandBus
Another command annotation processing thing

# CommandBus
Another command annotation processing thing

### format
```
/command subCommand flag1:345 flag2:false flag3:someString flag4:some String with spaces flag5:45.23
```
Where the ```/command subCommand``` directs to the desired Method (annotated with the @Command annotation)

Any flags following the above portion can be queried/accessed via the CommandEvent, or can be specified as a method parameter
via the @Arg annotation


### code
```java
class SomeClass
{
    @Command(alias = "command subCommand1")
    public void exampleCommand1(CommandEvent<?> event)
    {
        event.ifPresent("flag1", f -> System.out.println(f.number()));
        event.ifPresent("flag2", f -> System.out.println(f.bool()));
        ...etc
    }

    @Command(alias = "command subCommand2")
    public void exampleCommand2(@Caller CommandSource source, @Arg(a="flag1")int flag1, @Arg(a="flag4")String flag4)
    {
        System.out.println(source);
        System.out.println("Flag1: " + flag1);
        System.out.println("Flag4: " + flag4);
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
