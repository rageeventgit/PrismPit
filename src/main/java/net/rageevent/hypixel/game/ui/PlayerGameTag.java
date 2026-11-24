package net.rageevent.hypixel.game.ui;

import dev.prism.api.entity.Player;
import dev.prism.api.scoreboard.Team;
import dev.prism.api.scoreboard.TeamDisplay;
import dev.prism.api.scoreboard.Teams;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import lombok.experimental.UtilityClass;
import net.rageevent.hypixel.player.UserData;

import java.util.Optional;

@UtilityClass
public class PlayerGameTag {

    public void createTeam(Player player, Teams teams) {
        UserData userData = player.pitData();
        teams.teamOf(player).ifPresent(team -> teams.remove(team.name()));

        teams.create(teamName(player, userData))
                .display(format(userData))
                .add(player);
    }

    public void removeTeam(Player player, Teams teams) {
        UserData userData = player.pitData();
        teams.remove(teamName(player, userData));
    }

    public Optional<Team> get(Player player, Teams teams) {
        UserData userData = player.pitData();
        return teams.teamOf(teamName(player, userData));
    }


    private String teamName(Player player, UserData data) {
        return String.format("%02d%03d", data.rank().ordinal(), 999 - data.level()) + player.uuid();
    }

    public TeamDisplay format(UserData userData) {
        TextColor bracketColor = userData.prestige().bracketColor();
        return TeamDisplay.of(userData.rank().color())
                .prefix(Text.of("[", bracketColor)
                        .append(userData.levelText())
                        .append(Text.of("]", bracketColor))
                        .appendSpace())
                .suffix(userData.bounty() > 0 ? Text.of(userData.bounty() + "g", TextColor.GOLD).bold() : Text.EMPTY);
    }
}
