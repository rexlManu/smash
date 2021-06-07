package de.rexlmanu.smash.utility.menu;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface MenuAction {

    MenuAction EMPTY = event -> {};

    void handle(InventoryClickEvent event);

}
