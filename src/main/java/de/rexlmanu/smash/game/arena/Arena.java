package de.rexlmanu.smash.game.arena;

import de.rexlmanu.smash.utility.location.PaperLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Accessors(fluent = true)
@Data
@Builder
public class Arena {
    public static Arena create(String name) {
        return new Arena(name);
    }

    private String name;

    private List<ArenaLocation> respawns, itemSpawns;

    public Arena(String name) {
        this.name = name;
        this.respawns = new ArrayList<>();
        this.itemSpawns = new ArrayList<>();
    }

    public Optional<ArenaLocation> getRandomRespawnLocation() {
        return this.respawns.stream().min(Comparator.comparingInt(o -> PaperLocation.of(o.toBukkitLocation()).getNearbyPlayers(3).size()));
    }
}
