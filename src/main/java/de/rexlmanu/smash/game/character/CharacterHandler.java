package de.rexlmanu.smash.game.character;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Accessors(fluent = true)
public class CharacterHandler {

    @Getter
    private List<Character> characters;

    public CharacterHandler() {
        this.characters = new ArrayList<>();
    }

    public void register(Character character) {
        this.characters.add(character);
    }

    public Character random() {
        return this.characters.get(ThreadLocalRandom.current().nextInt(this.characters.size()));
    }
}
