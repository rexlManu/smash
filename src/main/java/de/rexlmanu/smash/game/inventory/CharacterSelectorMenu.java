package de.rexlmanu.smash.game.inventory;

import de.rexlmanu.smash.SmashPlugin;
import de.rexlmanu.smash.game.character.Character;
import de.rexlmanu.smash.game.events.UserSelectedCharacterEvent;
import de.rexlmanu.smash.game.user.GameUser;
import de.rexlmanu.smash.utility.item.ItemStackBuilder;
import de.rexlmanu.smash.utility.menu.MenuCreator;
import de.rexlmanu.smash.utility.menu.MenuItem;
import de.rexlmanu.smash.utility.menu.SingleMenu;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CharacterSelectorMenu extends SingleMenu {

    private Map<ItemStack, Character> itemStackCharacterMap;

    private SmashPlugin plugin;
    private Map<Character, ItemStack> characterHead;

    public CharacterSelectorMenu(SmashPlugin plugin) {
        super(plugin);
        this.plugin = plugin;

        this.itemStackCharacterMap = new HashMap<>();
        this.characterHead = new HashMap<>();

        this.plugin
                .characterHandler()
                .characters()
                .forEach(character ->
                        this.characterHead.put(character, ItemStackBuilder.customSkull(character.texture()).build()));
    }

    @Override
    public MenuCreator build(Player player) {
        MenuCreator creator = MenuCreator
                .create()
                .rows(3);
        GameUser gameUser = this.plugin.userManager().find(player.getUniqueId());
        this.plugin.characterHandler().characters().forEach(character -> {
            ItemStack itemStack = ItemStackBuilder
                    .of(this.characterHead.get(character).clone())
                    .name(gameUser.message(String.format("character.%s.name", character.name())))
                    .lore(gameUser.messageList(String.format("character.%s.description", character.name())))
                    .build();
            this.itemStackCharacterMap.put(itemStack, character);
            creator.addItem(MenuItem.of(itemStack), event -> {
                Character selectedCharacter = this.itemStackCharacterMap.get(event.getCurrentItem());
                GameUser user = this.plugin.userManager().find(event.getWhoClicked().getUniqueId());
                if (selectedCharacter.equals(user.selectedCharacter())) {
                    user.sendMessage("message.already-selected-the-character", character.name());
                    return;
                }
                user.selectedCharacter(selectedCharacter);
                Bukkit.getPluginManager().callEvent(new UserSelectedCharacterEvent(user, selectedCharacter));
                user.sendMessage("message.selected-the-character", character.name());
                user.sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.8f);
            });
        });
        return creator;
    }

    @Override
    public String inventoryName(Player player) {
        return this.plugin.userManager().find(player.getUniqueId()).message("inventory.character-selector.name");
    }

    @Override
    public void onRemove(Inventory inventory, Player player) {
        Arrays.stream(inventory.getContents()).filter(Objects::nonNull).forEach(itemStack -> this.itemStackCharacterMap.remove(itemStack));
    }
}
