package de.rexlmanu.smash.configuration;

import de.rexlmanu.smash.configuration.element.ConfigurationItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationProvider {

    private static List<ConfigurationEntry<?>> configurationEntries = new ArrayList<>();

    public static ConfigurationEntry<Integer> PLAYER_LIMIT = register("player-limit", 4);
    public static ConfigurationEntry<Integer> MIN_PLAYER = register("min-player-to-start", 2);
    public static ConfigurationEntry<Integer> LOBBY_COUNTDOWN = register("lobby-countdown", 5);
    public static ConfigurationEntry<Location> SPAWN_LOCATION = register("spawn-location", null);
    public static ConfigurationEntry<ConfigurationItem> ARENA_SELECTOR_ITEM = register("arena-selector-item", new ConfigurationItem(4, "item.arena-selector", Material.PAPER));
    public static ConfigurationEntry<ConfigurationItem> CHARACTER_SELECTOR_ITEM = register("character-selector-item", new ConfigurationItem(0, "item.character-selector", Material.NETHER_STAR));
    public static ConfigurationEntry<ConfigurationItem> BACK_TO_LOBBY_ITEM = register("back-to-lobby", new ConfigurationItem(8, "item.back-to-lobby", Material.IRON_DOOR));
    public static ConfigurationEntry<String> LOBBY_CHAT_FORMAT = register("lobby-chat-format", "&7%player &8» &7%message");
    public static ConfigurationEntry<Integer> LIVES = register("lives", 3);
    public static ConfigurationEntry<Double> DOUBLE_JUMP_FACTOR = register("double-jump-factor", 2.2d);
    public static ConfigurationEntry<Double> DOUBLE_JUMP_HEIGHT = register("double-jump-height", 0.5);
    public static ConfigurationEntry<Integer> DOUBLE_JUMP_COOLDOWN = register("double-jump-cooldown", 3);
    public static ConfigurationEntry<Integer> KNOCK_BACK_FACTOR = register("knock-back-factor", 70);
    public static ConfigurationEntry<Double> KNOCK_BACK_HEIGHT = register("knock-back-height", 0.3d);
    public static ConfigurationEntry<Integer> SMASH_COOLDOWN = register("smash-cooldown", 5);

    static {
        configurationEntries = new ArrayList<>();
    }

    static <T> ConfigurationEntry<T> register(String name, T defaultValue) {
        ConfigurationEntry<T> configurationEntry = new ConfigurationEntry<>(name, defaultValue);
        configurationEntries.add(configurationEntry);
        return configurationEntry;
    }

    private File file;
    private FileConfiguration configuration;

    public ConfigurationProvider(Path path) {
        this.file = path.resolve("configuration.yml").toFile();
        this.configuration = YamlConfiguration.loadConfiguration(this.file);

        this.configuration.options().copyDefaults(true);
        configurationEntries.forEach(entry -> configuration.addDefault(entry.name(), entry.value()));
        this.save();
    }

    private void save() {
        try {
            this.configuration.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> T get(ConfigurationEntry<T> entry) {
        return (T) this.configuration.get(entry.name(), entry.value());
    }

    public <T> void set(ConfigurationEntry<T> entry, T value) {
        this.configuration.set(entry.name(), value);
        this.save();
    }
}
