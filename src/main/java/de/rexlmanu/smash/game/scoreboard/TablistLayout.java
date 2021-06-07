package de.rexlmanu.smash.game.scoreboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Accessors(fluent = true)
@AllArgsConstructor
@Data
@Builder
public class TablistLayout {

    private String prefix, suffix;
    private ChatColor chatColor;
    private Map<Team.Option, Team.OptionStatus> options;

    public TablistLayout(String prefix, String suffix, ChatColor chatColor) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.chatColor = chatColor;
        this.options = new HashMap<>();
    }

    public TablistLayout option(@NotNull Team.Option option, @NotNull Team.OptionStatus status) {
        this.options.put(option, status);
        return this;
    }
}
