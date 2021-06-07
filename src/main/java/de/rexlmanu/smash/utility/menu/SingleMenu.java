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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SingleMenu implements Listener {

    private JavaPlugin plugin;

    private List<Inventory> inventories;
    private Map<Player, MenuCreator> playerCreator;

    public SingleMenu(JavaPlugin plugin) {
        this.plugin = plugin;

        this.inventories = new ArrayList<>();
        this.playerCreator = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public abstract MenuCreator build(Player player);

    public abstract String inventoryName(Player player);

    public void open(Player player) {
        MenuCreator creator = this.build(player);
        this.playerCreator.put(player, creator);
        var inventory = Bukkit.createInventory(null, creator.rows() * 9, this.inventoryName(player));
        this.inventories.add(inventory);
        this.setItems(inventory, creator);
        player.openInventory(inventory);
    }

    private void setItems(Inventory inventory, MenuCreator creator) {
        creator.itemActions().forEach((menuItem, action) -> {
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
                || !this.playerCreator.containsKey(event.getWhoClicked())
        ) return;

        event.setCancelled(true);

        this.playerCreator.get(event.getWhoClicked())
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

        this.playerCreator.remove(event.getPlayer());
        this.inventories.remove(closedInventory);

        this.onRemove(closedInventory, (Player) event.getPlayer());
    }

    public void onRemove(Inventory inventory, Player player) {

    }

    public void unregister() {
        this.inventories.forEach(inventory -> inventory.getViewers().forEach(HumanEntity::closeInventory));
        HandlerList.unregisterAll(this);
    }
}
