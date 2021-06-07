package de.rexlmanu.smash.utility.menu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
@Accessors(fluent = true)
@Data
public class MenuItem {

    public static MenuItem of(ItemStack itemStack, int slot) {
        return new MenuItem(itemStack, slot);
    }

    public static MenuItem of(ItemStack itemStack) {
        return new MenuItem(itemStack, -1);
    }

    private ItemStack itemStack;
    private int slot;

}
