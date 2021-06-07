package de.rexlmanu.smash.game.character.entities;

import de.rexlmanu.smash.game.character.Character;

public class YoshiCharacter implements Character {
    @Override
    public String name() {
        return "yoshi";
    }

    @Override
    public String texture() {
        return "https://textures.minecraft.net/texture/169d39f88df906d87582379a78ef26c987fda3897cb7974744c0fedc2524";
    }
}
