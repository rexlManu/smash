package de.rexlmanu.smash.game.event;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public interface EventListener<E> extends @NotNull Listener {

    @EventHandler
    void handle(E e);

}
