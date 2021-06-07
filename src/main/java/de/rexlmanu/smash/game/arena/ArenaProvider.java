package de.rexlmanu.smash.game.arena;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Accessors(fluent = true)
public class ArenaProvider {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private Path arenaDirectory;
    @Getter
    private List<Arena> arenas;

    public ArenaProvider(File dataFolder) {
        this.arenaDirectory = dataFolder.toPath().resolve("arenas");
        this.arenas = new ArrayList<>();

        this.arenaDirectory.toFile().mkdir();

        this.loadArenas();
    }

    public boolean register(Arena arena) {
        if (this.arenas.stream().anyMatch(a -> a.name().equals(arena.name()))) {
            return false;
        }
        this.arenas.add(arena);
        this.save(arena);
        return true;
    }

    public void save(Arena arena) {
        try {
            Files.writeString(
                    arenaDirectory.resolve(arena.name() + ".json"),
                    GSON.toJson(arena),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadArenas() {
        try {
            Files.walk(this.arenaDirectory).filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try {
                    this.arenas.add(GSON.fromJson(Files.readString(path), Arena.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Arena find(String name) {
        return this.arenas.stream().filter(arena -> arena.name().equals(name)).findFirst().orElse(null);
    }

    public void delete(Arena arena) {
        try {
            Files.deleteIfExists(arenaDirectory.resolve(arena.name() + ".json"));
            this.arenas.remove(arena);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
