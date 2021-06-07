package de.rexlmanu.smash.command;

import de.rexlmanu.smash.command.validation.EditorEnabled;
import de.rexlmanu.smash.configuration.ConfigurationProvider;
import de.rexlmanu.smash.game.arena.Arena;
import de.rexlmanu.smash.game.arena.ArenaLocation;
import de.rexlmanu.smash.game.arena.ArenaProvider;
import de.rexlmanu.smash.game.state.GameProvider;
import de.rexlmanu.smash.game.state.GameState;
import de.rexlmanu.smash.game.user.GameUser;
import de.rexlmanu.smash.utility.location.PaperLocation;
import io.github.revxrsal.cub.annotation.Command;
import io.github.revxrsal.cub.annotation.Dependency;
import io.github.revxrsal.cub.annotation.Description;
import io.github.revxrsal.cub.annotation.Subcommand;
import org.bukkit.World;

@Command("smash")
@Description("The main command for smash")
public class SmashCommand {

    @Dependency
    private ConfigurationProvider configurationProvider;
    @Dependency
    private GameProvider gameProvider;

    @Subcommand("setspawn")
    @Description("Set the spawn")
    public String setSpawn(GameUser gameUser) {
        configurationProvider.set(ConfigurationProvider.SPAWN_LOCATION, gameUser.asPlayer().getLocation());
        return gameUser.message("command.smash.set-spawn");
    }

    @Subcommand("editor")
    @Description("Toggle the editor mode for creating / editing arenas")
    public void editor(GameUser gameUser) {
        if (!this.gameProvider.state().equals(GameState.LOBBY)) {
            gameUser.sendMessage("message.only-in-lobby-available");
            return;
        }
        if (gameUser.editor()) gameUser.leaveEditor();
        else gameUser.enableEditor();
    }

    @Subcommand("arena")
    @Description("The arena related commands")
    public static class ArenaCommand {
        @Dependency
        private ArenaProvider arenaProvider;

        @Subcommand("create")
        public String create(@EditorEnabled GameUser user, String name) {
            if (arenaProvider.register(Arena.create(name))) {
                return user.message("message.arena-created", name);
            }
            return user.message("message.arena-already-exists", name);
        }

        @Subcommand("addrespawn")
        public String addRespawn(@EditorEnabled GameUser user, Arena arena) {
            arena.respawns().add(ArenaLocation.of(user.asPlayer().getLocation()));
            this.arenaProvider.save(arena);
            return user.message("message.arena-added-respawn", arena.name());
        }

        @Subcommand("additemspawn")
        public String addItemSpawn(@EditorEnabled GameUser user, Arena arena) {
            arena.itemSpawns().add(ArenaLocation.of(PaperLocation.of(user.asPlayer().getLocation()).toCenterLocation()));
            this.arenaProvider.save(arena);
            return user.message("message.arena-added-item-spawn", arena.name());
        }

        @Subcommand("delete")
        public String delete(@EditorEnabled GameUser user, Arena arena) {
            this.arenaProvider.delete(arena);
            return user.message("message.arena-deleted", arena.name());
        }
    }

    @Subcommand("tp")
    @Description("Tp to a world")
    public String tp(@EditorEnabled GameUser user, World world) {
        user.asPlayer().teleport(world.getSpawnLocation());
        return user.message("message.world-teleported", world.getName());
    }

}
