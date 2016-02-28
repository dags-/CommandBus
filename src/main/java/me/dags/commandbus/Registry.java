package me.dags.commandbus;

import me.dags.commandbus.command.CommandContainer;
import me.dags.commandbus.command.CommandEvent;
import me.dags.commandbus.platform.Registrar;

import java.util.*;
import java.util.stream.Collectors;

public class Registry
{
	private final RegistryNode register = new RegistryNode();
	private final List<String> suggestions = new ArrayList<>();
	private final Registrar registrar;

    protected Registry(Registrar registrar)
    {
        this.registrar = registrar;
    }

	protected void register(CommandBus commandBus, Object plugin, CommandContainer command)
	{
		RegistryNode target = register;
		int length = command.command().split(" ").length;

		for (int i = 0; i < length; i++)
		{
			List<String> all = new ArrayList<>();
			for (String s : command.aliases())
			{
				String[] split = s.split(" ");
				if (split.length > i)
				{
					all.add(split[i]);
				}
			}

            if (all.isEmpty())
            {
                return;
            }

			RegistryNode node = target.getOrCreate(all.get(0));
			for (String a : all) target.addNode(a, node);
            if (i == 0)
            {
                registrar.register(plugin, commandBus, command, all);
            }
			target = node;
		}
		target.add(command);

        Collections.addAll(suggestions, command.aliases());
	}

	public List<String> listSuggestions(String in)
	{
		String match = in.toLowerCase();
		return suggestions.stream().filter(s -> s.startsWith(match)).collect(Collectors.toList());
	}

	public List<String> listInfo(String in)
	{
		List<String> info = new ArrayList<>();
		for (String s : listSuggestions(in))
		{
            info.addAll(get(s).stream().map(CommandContainer::toString).collect(Collectors.toList()));
		}
		return info;
	}

    public List<String> complete(String in)
    {
        return suggestions.stream()
                .filter(s -> s.startsWith(in))
                .map(s -> {
                    int index = in.lastIndexOf(" ") + 1;
                    return index > 0 && index < s.length() ? s.substring(index) : s.replace(in, "");
                })
                .collect(Collectors.toList());
    }

	public List<String> listInfo()
	{
		return listInfo("");
	}

	protected Optional<CommandContainer> matchOne(CommandEvent<?> event)
	{
		for (CommandContainer c : get(event.command()))
		{
			if (c.exactMatchFor(event))
			{
				return Optional.of(c);
			}
            else
            {
                System.out.println(c + " does not match " + event);
            }
		}
		return Optional.empty();
	}

    protected List<CommandContainer> matchAny(CommandEvent<?> event)
    {
        return get(event.command()).stream().filter(c -> c.matchFor(event)).collect(Collectors.toList());
    }

	protected List<CommandContainer> get(String path)
	{
		return get(path.split(" "));
	}

	protected List<CommandContainer> get(String...path)
	{
		RegistryNode node = register;
		for (String s : path)
		{
			node = node.getNode(s);
			if (node == null)
			{
				List<CommandContainer> result = Collections.emptyList();
				return result;
			}
		}
		return node.commands();
	}
}
