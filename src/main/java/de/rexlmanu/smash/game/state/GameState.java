package de.rexlmanu.smash.game.state;

public enum GameState {

    LOBBY,
    PLAYING,
    END;

    public static GameState defaultState() {
        return LOBBY;
    }
}
