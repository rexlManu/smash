package de.rexlmanu.smash.game.scoreboard;

import com.google.inject.Inject;
import de.rexlmanu.smash.SmashPlugin;
import de.rexlmanu.smash.game.user.GameUser;
import de.rexlmanu.smash.game.user.UserManager;
import de.rexlmanu.smash.utility.fastboard.FastBoard;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardManager {

    private SmashPlugin plugin;

    private UserManager userManager;

    @Inject
    public ScoreboardManager(SmashPlugin plugin) {
        this.plugin = plugin;
        this.userManager = plugin.userManager();
    }

    public void update(ScoreboardCreator creator) {
        this.userManager.users().forEach(user -> this.updateSingle(user, creator));
    }

    public void updateSingle(GameUser user, ScoreboardCreator creator) {
        this.updateSidebar(user, creator);
        this.updateTablist(user, creator);
    }

    public void updateSidebar(GameUser user, ScoreboardCreator creator) {
        if (user.fastBoard() == null) {
            user.fastBoard(new FastBoard(user.asPlayer()));
        }
        SidebarLayout sidebarLayout = creator.getSidebarLayout(user);
        user.fastBoard().updateTitle(user.message(sidebarLayout.title()));
        user.fastBoard().updateLines(sidebarLayout.lines());
    }

    public void updateTablist(GameUser user, ScoreboardCreator creator) {
        Scoreboard scoreboard = user.asPlayer().getScoreboard();

        this.userManager.users().forEach(target -> {
            TablistLayout tablistLayout = creator.getTablistLayout(target, user.language());
            if (scoreboard.getTeam(target.scoreboardTeamName()) == null) {
                scoreboard.registerNewTeam(target.scoreboardTeamName());
            }
            Team team = scoreboard.getTeam(target.scoreboardTeamName());
            team.setPrefix(tablistLayout.prefix());
            team.setSuffix(tablistLayout.suffix());
            team.setColor(tablistLayout.chatColor());
            tablistLayout.options().forEach(team::setOption);
            team.addEntry(target.asPlayer().getName());
        });
    }
}
