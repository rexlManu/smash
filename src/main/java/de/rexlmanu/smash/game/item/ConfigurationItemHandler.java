package de.rexlmanu.smash.game.item;

import de.rexlmanu.smash.configuration.element.ConfigurationItem;
import de.rexlmanu.smash.game.user.GameUser;
import de.rexlmanu.smash.utility.item.ItemStackBuilder;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class ConfigurationItemHandler {

    public static ItemStack createItem(GameUser user, ConfigurationItem item) {
        return ItemStackBuilder
                .of(item.material())
                .name(user.message(item.nameTranslation()))
                .lore(user.messageList(item.loreTranslation()))
                .build();
    }

}
