package de.rexlmanu.smash.utility;

import de.rexlmanu.smash.utility.location.PaperLocation;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.stream.IntStream;

public class PlayerUtils {

    public static void resetPlayer(Player player) {
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.setHealth(20);
        player.setHealthScale(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
        player.setFoodLevel(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(false);
        player.setVelocity(new Vector(0, 0, 0));
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public static boolean isOnGround(Player player) {
        return !PaperLocation.of(player.getLocation()).subtract(0, 1, 0).getBlock().getType().equals(Material.AIR);
    }

    public static boolean isAir(Location location) {
        return location.getBlock().getType().equals(Material.AIR);
    }

    public static boolean isBlocksInAir(Player player, int blockCount) {
        return IntStream.range(0, blockCount).allMatch(value -> isAir(PaperLocation.of(player.getLocation()).toBlockLocation().subtract(0, value, 0)));
    }
}
