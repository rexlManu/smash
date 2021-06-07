package de.rexlmanu.smash.game.scoreboard;

import de.rexlmanu.smash.game.user.GameUser;
import de.rexlmanu.smash.language.Language;

import java.util.List;

public interface ScoreboardCreator {

    SidebarLayout getSidebarLayout(GameUser user);

    TablistLayout getTablistLayout(GameUser user, Language language);

}
