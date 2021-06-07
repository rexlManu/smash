package de.rexlmanu.smash.utility.menu;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

@Accessors(fluent = true, chain = true)
@Getter
public class MenuCreator {

    public static MenuCreator create() {
        return new MenuCreator();
    }

    @Setter
    private int rows;

    private Map<MenuItem, MenuAction> itemActions;

    public MenuCreator() {
        this.rows = 1;
        this.itemActions = new HashMap<>();
    }

    public MenuCreator addItem(MenuItem menuItem, MenuAction action) {
        this.itemActions.put(menuItem, action);
        return this;
    }
}
