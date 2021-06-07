package de.rexlmanu.smash;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import de.rexlmanu.smash.command.SmashCommand;
import de.rexlmanu.smash.command.SmashCommandExceptionHandler;
import de.rexlmanu.smash.command.validation.EditorEnabled;
import de.rexlmanu.smash.configuration.ConfigurationProvider;
import de.rexlmanu.smash.game.GameEventListener;
import de.rexlmanu.smash.game.arena.Arena;
import de.rexlmanu.smash.game.arena.ArenaProvider;
import de.rexlmanu.smash.game.character.CharacterHandler;
import de.rexlmanu.smash.game.character.entities.MarioCharacter;
import de.rexlmanu.smash.game.character.entities.YoshiCharacter;
import de.rexlmanu.smash.game.scoreboard.ScoreboardManager;
import de.rexlmanu.smash.game.state.GameProvider;
import de.rexlmanu.smash.game.state.GameState;
import de.rexlmanu.smash.game.state.statelisteners.EndStateListener;
import de.rexlmanu.smash.game.state.statelisteners.LobbyStateListener;
import de.rexlmanu.smash.game.state.statelisteners.PlayingStateListener;
import de.rexlmanu.smash.game.user.GameUser;
import de.rexlmanu.smash.game.user.UserManager;
import de.rexlmanu.smash.language.LanguageHandler;
import io.github.revxrsal.cub.bukkit.BukkitCommandHandler;
import io.github.revxrsal.cub.exception.InvalidValueException;
import io.github.revxrsal.cub.exception.SimpleCommandException;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

@Accessors(fluent = true)
public final class SmashPlugin extends JavaPlugin implements Module {
    private ConfigurationProvider configurationProvider;
    private LanguageHandler languageHandler;
    private ArenaProvider arenaProvider;
    private GameProvider gameProvider;

    private BukkitCommandHandler commandHandler;

    private Injector injector;

    @Getter
    private CharacterHandler characterHandler;
    @Getter
    private UserManager userManager;
    @Getter
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        this.getDataFolder().mkdir();
        // Delete all language files for better developement enviroment feeling
        if (true) {
            try {
                Files.walk(this.getDataFolder().toPath().resolve("lang"))
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.configurationProvider = new ConfigurationProvider(this.getDataFolder().toPath());
        this.languageHandler = new LanguageHandler(this.getDataFolder());
        this.arenaProvider = new ArenaProvider(this.getDataFolder());
        this.gameProvider = new GameProvider();

        this.commandHandler = BukkitCommandHandler.create(this);

        this.injector = Guice.createInjector(this);
        this.injector.injectMembers(this);

        this.characterHandler = new CharacterHandler();
        this.userManager = this.injector.getInstance(UserManager.class);
        this.scoreboardManager = this.injector.getInstance(ScoreboardManager.class);

        this.characterHandler.register(this.injector.getInstance(MarioCharacter.class));
        this.characterHandler.register(this.injector.getInstance(YoshiCharacter.class));

        LobbyStateListener instance = this.injector.getInstance(LobbyStateListener.class);
        this.gameProvider.register(GameState.LOBBY, instance);
        this.gameProvider.register(GameState.PLAYING, this.injector.getInstance(PlayingStateListener.class));
        this.gameProvider.register(GameState.END, this.injector.getInstance(EndStateListener.class));

        this.gameProvider.setGame(GameState.defaultState());

        Bukkit.getPluginManager().registerEvents(new GameEventListener(), this);

        this.commandHandler
                .setHelpWriter((command, subject, args) -> command.getDescription())
                .registerDependency(ConfigurationProvider.class, this.configurationProvider)
                .registerDependency(GameProvider.class, this.gameProvider)
                .registerDependency(ArenaProvider.class, this.arenaProvider)
                .registerResponseHandler(String.class, (response, subject, command, context) -> subject.reply(response))
                .registerContextResolver(GameUser.class, (args, subject, parameter) -> this.userManager.find(subject.getUUID()))
                .registerTypeResolver(Arena.class, (args, subject, parameter) -> {
                    String name = args.pop();
                    Arena arena = this.arenaProvider.find(name);
                    if (arena == null)
                        throw new InvalidValueException(new InvalidValueException.ValueType("arena"), name);
                    return arena;
                })
                .registerTypeResolver(World.class, (args, subject, parameter) -> {
                    String name = args.pop();
                    World world = Bukkit.getWorld(name);
                    if (world == null)
                        throw new InvalidValueException(new InvalidValueException.ValueType("world"), name);
                    return world;
                })
                .registerParameterValidator(GameUser.class, (value, parameter, subject) -> {
                    if (parameter.getAnnotation(EditorEnabled.class) == null) return;
                    if (!value.editor()) throw new SimpleCommandException(value.message("message.need-to-be-editor"));
                    if (!gameProvider.state().equals(GameState.LOBBY))
                        throw new SimpleCommandException(value.message("message.only-in-lobby-available"));
                })
                .setExceptionHandler(new SmashCommandExceptionHandler(this.userManager));

        this.commandHandler.registerParameterTab(Arena.class, (args, sender, command, bukkitCommand) -> this.arenaProvider.arenas().stream().map(Arena::name).collect(Collectors.toList()));
        this.commandHandler.registerCommand(new SmashCommand());
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(SmashPlugin.class).toInstance(this);
        binder.bind(JavaPlugin.class).toInstance(this);
        binder.bind(ConfigurationProvider.class).toInstance(this.configurationProvider);
        binder.bind(LanguageHandler.class).toInstance(this.languageHandler);
        binder.bind(GameProvider.class).toInstance(this.gameProvider);
        binder.bind(ArenaProvider.class).toInstance(this.arenaProvider);
    }
}
