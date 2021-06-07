package de.rexlmanu.smash.game.state;

import com.google.inject.Inject;
import de.rexlmanu.smash.game.event.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class GameStateListener implements Listener {

    public abstract void onBegin();

    public abstract void onEnd();

}
