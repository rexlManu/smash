package de.rexlmanu.smash.game.user;

import de.rexlmanu.smash.game.arena.Arena;
import de.rexlmanu.smash.game.character.Character;
import de.rexlmanu.smash.language.Language;
import de.rexlmanu.smash.utility.PlayerUtils;
import de.rexlmanu.smash.utility.fastboard.FastBoard;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public class GameUser {

    @Setter
    private FastBoard fastBoard;
    private String scoreboardTeamName;
    private boolean editor;
    @Setter
    private Arena votedArena;
    @Setter
    private Character selectedCharacter;
    @Setter
    private int live;
    @Setter
    private int damage;
    @Setter
    private boolean spectator = false;

    private UUID uuid;
    @Setter
    private Language language;

    public GameUser(UUID uuid, Language language) {
        this.uuid = uuid;
        this.language = language;

        this.scoreboardTeamName = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        this.reset();
    }

    public String message(String key, Object... parameters) {
        if (parameters.length == 0) return this.language.translate(key);
        return this.language.translate(key, parameters);
    }

    public List<String> messageList(String key) {
        return this.language.translateList(key);
    }

    public void sendMessage(String key, Object... parameters) {
        this.asPlayer().sendMessage(this.message(key, parameters));
    }

    public Player asPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public void leaveGame() {
        // Simple solution
        this.asPlayer().kickPlayer("user.leave()");
    }

    public void enableEditor() {
        this.editor = true;
        this.sendMessage("message.editor-enabled");
        PlayerUtils.resetPlayer(this.asPlayer());
    }

    public void leaveEditor() {
        this.editor = false;
        this.sendMessage("message.editor-disabled");
        PlayerUtils.resetPlayer(this.asPlayer());
    }

    public void reset() {
        this.votedArena = null;
        this.selectedCharacter = null;
        this.live = 0;
        this.damage = 0;
        this.editor = false;
    }

    public void sound(Sound sound, float pitch) {
        Player player = this.asPlayer();
        player.playSound(player.getLocation(), sound, 1, pitch);
    }
}
