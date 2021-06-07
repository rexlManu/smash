package de.rexlmanu.smash.game.events;

import de.rexlmanu.smash.game.character.Character;
import de.rexlmanu.smash.game.user.GameUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Accessors(fluent = true)
@Getter
public class UserSelectedCharacterEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private GameUser user;
    private Character character;

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
