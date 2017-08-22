package me.dags.commandbus;

import org.spongepowered.api.CatalogType;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public interface AliasCatalogType extends CatalogType {

    List<String> getAliases();
}
