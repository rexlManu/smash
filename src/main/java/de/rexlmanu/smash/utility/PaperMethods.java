package de.rexlmanu.smash.utility;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * So this class exists to port paper methods to spigot because i dont know if
 * the end user of this plugin will use the paper api.
 * <p>
 * I really hate this situation. Now I'm will have potential users with paper and normal spigot and I cant use
 * the paper functions that improves my life
 */
public class PaperMethods {

    public static <T extends Entity> Collection<T> getNearbyEntitiesByType(World world, @Nullable Class<? extends Entity> clazz, @NotNull final Location loc, final double xRadius, final double yRadius, final double zRadius, @Nullable final Predicate<T> predicate) {
        if (clazz == null) {
            clazz = Entity.class;
        }
        final List<T> nearby = new ArrayList<T>();
        for (final Entity bukkitEntity : world.getNearbyEntities(loc, xRadius, yRadius, zRadius)) {
            if (clazz.isAssignableFrom(bukkitEntity.getClass()) && (predicate == null || predicate.test((T)bukkitEntity))) {
                nearby.add((T)bukkitEntity);
            }
        }
        return nearby;
    }

}
