package de.rexlmanu.smash.game.state;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import de.rexlmanu.smash.SmashPlugin;
import de.rexlmanu.smash.game.user.UserManager;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Accessors(fluent = true)
public class GameProvider {

    @Inject
    private SmashPlugin plugin;

    private Map<GameState, GameStateListener> gameStateListeners;
    @Getter
    private GameState state = null;
    private GameStateListener listener = null;

    public GameProvider() {
        this.gameStateListeners = Maps.newHashMap();
    }

    public Optional<GameStateListener> findListener(GameState gameState) {
        return Optional.ofNullable(this.gameStateListeners.get(gameState));
    }

    public void register(GameState gameState, GameStateListener gameStateListener) {
        this.gameStateListeners.put(gameState, gameStateListener);
    }

    public void setGame(GameState gameState) {
        if (Objects.nonNull(this.listener)) {
            this.listener.onEnd();
            HandlerList.unregisterAll(this.listener);
        }
        this.state = gameState;
        this.listener = this.findListener(this.state).orElse(null);
        if (Objects.nonNull(this.listener)) {
            Bukkit.getPluginManager().registerEvents(this.listener, this.plugin);
            this.listener.onBegin();
        }
    }
}
