package de.rexlmanu.smash.game.character.entities;

import com.google.inject.Inject;
import de.rexlmanu.smash.game.character.Character;

public class MarioCharacter implements Character {

    @Inject
    public MarioCharacter() {
    }

    @Override
    public String name() {
        return "mario";
    }

    @Override
    public String texture() {
        return "https://textures.minecraft.net/texture/13d5642ab6ff1743210d5cabe696343ab822e3ad6bd95b1db0f8b9e32a7635";
    }
}
