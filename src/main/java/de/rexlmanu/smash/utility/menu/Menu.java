package de.rexlmanu.smash.utility.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public abstract class Menu implements Listener {

    private JavaPlugin plugin;

    private MenuCreator creator = null;

    private List<Inventory> inventories;

    public Menu(JavaPlugin plugin) {
        this.plugin = plugin;

        this.inventories = new ArrayList<>();

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public abstract MenuCreator build();

    public abstract String inventoryName(Player player);

    public void open(Player player) {
        var inventory = Bukkit.createInventory(null, this.creator.rows() * 9, this.inventoryName(player));
        this.inventories.add(inventory);
        this.setItems(inventory);
        player.openInventory(inventory);
    }

    private void setItems(Inventory inventory) {
        this.creator.itemActions().forEach((menuItem, action) -> {
            if (menuItem.slot() == -1) inventory.addItem(menuItem.itemStack());
            else inventory.setItem(menuItem.slot(), menuItem.itemStack());
        });
    }

    @EventHandler
    public void handle(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();
        if (
                clickedInventory == null
                        || !this.inventories.contains(clickedInventory)
                        || currentItem == null
                        || !(event.getWhoClicked() instanceof Player)
        ) return;

        event.setCancelled(true);

        this.creator
                .itemActions()
                .entrySet()
                .stream()
                .filter(menuItemMenuActionEntry -> menuItemMenuActionEntry.getKey().itemStack().equals(currentItem)).findFirst()
                .ifPresent(menuItemMenuActionEntry -> menuItemMenuActionEntry.getValue().handle(event));
    }

    @EventHandler
    public void handle(InventoryCloseEvent event) {
        Inventory closedInventory = event.getInventory();

        if (!this.inventories.contains(closedInventory)) return;

        this.inventories.remove(closedInventory);
    }

    public void unregister() {
        this.inventories.forEach(inventory -> inventory.getViewers().forEach(HumanEntity::closeInventory));
        HandlerList.unregisterAll(this);
    }

    public void buildCreator() {
        this.creator = this.build();
    }
}
