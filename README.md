# CommandBus
Another command annotation processing thing

### example code
```java
public void derp(Object pluginInstance)
{
    new CommandBus(pluginInstance).register(Some.class).submitCommands();
}
```
