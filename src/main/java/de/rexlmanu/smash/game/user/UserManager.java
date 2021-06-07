package de.rexlmanu.smash.game.user;

import com.google.inject.Inject;
import de.rexlmanu.smash.game.events.UserChangeLocaleEvent;
import de.rexlmanu.smash.language.Language;
import de.rexlmanu.smash.language.LanguageHandler;
import eu.miopowered.packetlistener.PacketHandler;
import eu.miopowered.packetlistener.PacketListener;
import eu.miopowered.packetlistener.filter.PacketFilter;
import eu.miopowered.packetlistener.filter.PacketState;
import eu.miopowered.packetlistener.filter.PacketType;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public class UserManager implements Listener {

    private JavaPlugin plugin;
    private LanguageHandler languageHandler;

    private List<GameUser> users;

    @Inject
    public UserManager(JavaPlugin plugin, LanguageHandler languageHandler) {
        this.plugin = plugin;
        this.languageHandler = languageHandler;
        this.users = new ArrayList<>();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public @NotNull GameUser find(UUID uuid) {
        return Objects.requireNonNull(this.users.stream().filter(gameUser -> gameUser.uuid().equals(uuid)).findFirst().orElse(null));
    }

    private void registerPlayer(Player player) {
        Language language = this.languageHandler.findLanguage(player.getLocale().toLowerCase());
        GameUser user = new GameUser(
                player.getUniqueId(),
                language
        );
        this.users.add(user);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handle(UserChangeLocaleEvent event) {
        event.user().language(this.languageHandler.findLanguage(event.locale().toLowerCase()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handle(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.registerPlayer(player);

        PacketHandler.listen(PacketListener.of(player).filter(PacketType.IN, PacketState.PLAY, PacketFilter.name("Settings")).receive((context, packet) -> {
            try {
                Field locale = packet.packet().getClass().getField("locale");
                locale.setAccessible(true);
                String newLocale = locale.get(packet.packet()).toString();
                GameUser user = this.find(player.getUniqueId());
                if (!newLocale.toLowerCase().equals(user.language().locale())) {
                    Bukkit.getScheduler().runTask(this.plugin, () -> Bukkit.getPluginManager().callEvent(new UserChangeLocaleEvent(user, newLocale)));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handle(PlayerQuitEvent event) {
        this.users.remove(this.find(event.getPlayer().getUniqueId()));

        PacketHandler.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handle(ServerLoadEvent event) {
        Bukkit.getOnlinePlayers().forEach(this::registerPlayer);
    }

    public boolean isSpectator(Player player) {
        return this.find(player.getUniqueId()).spectator();
    }
}
