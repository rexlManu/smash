package de.rexlmanu.smash.game.arena;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@AllArgsConstructor
@Accessors(fluent = true)
@Data
public class ArenaLocation {

    public static ArenaLocation of(Location location) {
        return new ArenaLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), location.getWorld().getName());
    }

    private double x, y, z;
    private float yaw, pitch;
    private String worldName;

    public Location toBukkitLocation() {
        return new Location(Bukkit.getWorld(worldName), this.x, this.y, this.z, this.yaw, this.pitch);
    }

}
