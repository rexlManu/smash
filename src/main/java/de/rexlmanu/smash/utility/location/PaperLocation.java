package de.rexlmanu.smash.utility.location;

import de.rexlmanu.smash.utility.PaperMethods;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Pretty much a port from paper
 */
public class PaperLocation extends Location {

    public static PaperLocation of(Location location) {
        return new PaperLocation(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public PaperLocation(@Nullable World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    public PaperLocation(@Nullable World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @NotNull
    public Collection<Entity> getNearbyEntities(final double x, final double y, final double z) {
        final World world = this.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("Location has no world");
        }
        return world.getNearbyEntities(this, x, y, z);
    }

    @NotNull
    public Collection<LivingEntity> getNearbyLivingEntities(final double radius) {
        return this.getNearbyEntitiesByType(LivingEntity.class, radius, radius, radius);
    }

    @NotNull
    public Collection<LivingEntity> getNearbyLivingEntities(final double xzRadius, final double yRadius) {
        return this.getNearbyEntitiesByType(LivingEntity.class, xzRadius, yRadius, xzRadius);
    }

    @NotNull
    public Collection<LivingEntity> getNearbyLivingEntities(final double xRadius, final double yRadius, final double zRadius) {
        return this.getNearbyEntitiesByType(LivingEntity.class, xRadius, yRadius, zRadius);
    }

    @NotNull
    public Collection<LivingEntity> getNearbyLivingEntities(final double radius, @Nullable final Predicate<LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(LivingEntity.class, radius, radius, radius, predicate);
    }

    @NotNull
    public Collection<LivingEntity> getNearbyLivingEntities(final double xzRadius, final double yRadius, @Nullable final Predicate<LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(LivingEntity.class, xzRadius, yRadius, xzRadius, predicate);
    }

    @NotNull
    public Collection<LivingEntity> getNearbyLivingEntities(final double xRadius, final double yRadius, final double zRadius, @Nullable final Predicate<LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(LivingEntity.class, xRadius, yRadius, zRadius, predicate);
    }

    @NotNull
    public Collection<Player> getNearbyPlayers(final double radius) {
        return this.getNearbyEntitiesByType(Player.class, radius, radius, radius);
    }

    @NotNull
    public Collection<Player> getNearbyPlayers(final double xzRadius, final double yRadius) {
        return this.getNearbyEntitiesByType(Player.class, xzRadius, yRadius, xzRadius);
    }

    @NotNull
    public Collection<Player> getNearbyPlayers(final double xRadius, final double yRadius, final double zRadius) {
        return this.getNearbyEntitiesByType(Player.class, xRadius, yRadius, zRadius);
    }

    @NotNull
    public Collection<Player> getNearbyPlayers(final double radius, @Nullable final Predicate<Player> predicate) {
        return this.getNearbyEntitiesByType(Player.class, radius, radius, radius, predicate);
    }

    @NotNull
    public Collection<Player> getNearbyPlayers(final double xzRadius, final double yRadius, @Nullable final Predicate<Player> predicate) {
        return this.getNearbyEntitiesByType(Player.class, xzRadius, yRadius, xzRadius, predicate);
    }

    @NotNull
    public Collection<Player> getNearbyPlayers(final double xRadius, final double yRadius, final double zRadius, @Nullable final Predicate<Player> predicate) {
        return this.getNearbyEntitiesByType(Player.class, xRadius, yRadius, zRadius, predicate);
    }

    @NotNull
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(@Nullable final Class<? extends T> clazz, final double radius) {
        return this.getNearbyEntitiesByType(clazz, radius, radius, radius, null);
    }

    @NotNull
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(@Nullable final Class<? extends T> clazz, final double xzRadius, final double yRadius) {
        return this.getNearbyEntitiesByType(clazz, xzRadius, yRadius, xzRadius, null);
    }

    @NotNull
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(@Nullable final Class<? extends T> clazz, final double xRadius, final double yRadius, final double zRadius) {
        return this.getNearbyEntitiesByType(clazz, xRadius, yRadius, zRadius, null);
    }

    @NotNull
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(@Nullable final Class<? extends T> clazz, final double radius, @Nullable final Predicate<T> predicate) {
        return this.getNearbyEntitiesByType(clazz, radius, radius, radius, predicate);
    }

    @NotNull
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(@Nullable final Class<? extends T> clazz, final double xzRadius, final double yRadius, @Nullable final Predicate<T> predicate) {
        return this.getNearbyEntitiesByType(clazz, xzRadius, yRadius, xzRadius, predicate);
    }

    @NotNull
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(@Nullable final Class<? extends Entity> clazz, final double xRadius, final double yRadius, final double zRadius, @Nullable final Predicate<T> predicate) {
        final World world = this.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("Location has no world");
        }
        return PaperMethods.getNearbyEntitiesByType(world, clazz, this, xRadius, yRadius, zRadius, predicate);
    }

    public Location toBlockLocation() {
        final Location blockLoc = this.clone();
        blockLoc.setX(this.getBlockX());
        blockLoc.setY(this.getBlockY());
        blockLoc.setZ(this.getBlockZ());
        return blockLoc;
    }

    public Location toCenterLocation() {
        return toBlockLocation().add(0.5, 0.5, 0.5);
    }

}
