package me.dags.commandbus;
import me.dags.commandbus.command.CommandContainer;

import java.util.*;

public class RegistryNode
{
	private final Map<String, RegistryNode> nodes = new HashMap<>();
	private final List<CommandContainer> commands = new ArrayList<>();

	public RegistryNode getOrCreate(String alias)
	{
		RegistryNode node = nodes.get(alias);
		if (node == null)
		{
			nodes.put(alias, node = new RegistryNode());
		}
		return node;
	}

    protected RegistryNode getNode(String alias)
	{
		return nodes.get(alias);
	}

    protected Set<RegistryNode> getChildren()
    {
        Set<RegistryNode> matches = new HashSet<>();
        getChildren(matches);
        System.out.println(matches.size());
        return matches;
    }

    protected List<CommandContainer> commands()
    {
        return commands;
    }

    protected boolean contains(String in)
    {
        return nodes.containsKey(in);
    }

    protected boolean contains(Collection<String> in)
    {
        for (String s : in)
        {
            if (nodes.containsKey(s))
            {
                return true;
            }
        }
        return false;
    }

    protected void addNode(String alias, RegistryNode node)
    {
        if (!nodes.containsKey(alias))
        {
            nodes.put(alias, node);
        }
    }

    protected void add(CommandContainer command)
	{
		commands.add(command);
	}

    protected void getChildren(Set<RegistryNode> matches)
	{
		for (RegistryNode node : nodes.values())
		{
			matches.add(node);
			node.getChildren(matches);
		}
	}

	@Override
	public String toString()
	{
		return nodes.toString();
	}
}
