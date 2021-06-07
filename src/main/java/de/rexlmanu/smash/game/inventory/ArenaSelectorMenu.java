package de.rexlmanu.smash.game.inventory;

import de.rexlmanu.smash.SmashPlugin;
import de.rexlmanu.smash.game.arena.Arena;
import de.rexlmanu.smash.game.arena.ArenaProvider;
import de.rexlmanu.smash.game.user.GameUser;
import de.rexlmanu.smash.utility.item.ItemStackBuilder;
import de.rexlmanu.smash.utility.menu.Menu;
import de.rexlmanu.smash.utility.menu.MenuCreator;
import de.rexlmanu.smash.utility.menu.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ArenaSelectorMenu extends Menu {

    private SmashPlugin plugin;
    private ArenaProvider arenaProvider;
    private Map<ItemStack, Arena> arenaVoteItem;

    public ArenaSelectorMenu(SmashPlugin plugin, ArenaProvider arenaProvider) {
        super(plugin);
        this.plugin = plugin;
        this.arenaProvider = arenaProvider;

        this.arenaVoteItem = new HashMap<>();

        this.buildCreator();
    }

    @Override
    public MenuCreator build() {
        MenuCreator creator = MenuCreator
                .create()
                .rows(3);
        this.arenaProvider.arenas().forEach(arena -> {
            ItemStack itemStack = ItemStackBuilder.of(Material.PAPER).name(ChatColor.RESET.toString() + ChatColor.GRAY + arena.name()).build();
            this.arenaVoteItem.put(itemStack, arena);
            creator.addItem(MenuItem.of(itemStack), event -> {
                Arena votedArena = this.arenaVoteItem.get(event.getCurrentItem());
                GameUser user = this.plugin.userManager().find(event.getWhoClicked().getUniqueId());
                if (votedArena.equals(user.votedArena())) {
                    user.sendMessage("message.already-voted-for-the-arena", votedArena.name());
                    return;
                }
                user.votedArena(votedArena);
                user.sendMessage("message.voted-for-the-arena", votedArena.name());
                user.sound(Sound.ENTITY_PLAYER_LEVELUP, 1.3f);
            });
        });
        return creator;
    }

    @Override
    public String inventoryName(Player player) {
        return this.plugin.userManager().find(player.getUniqueId()).message("inventory.arena-selector.name");
    }
}
