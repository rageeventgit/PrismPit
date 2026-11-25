package net.rageevent.hypixel.game.mechanic;

import dev.prism.api.entity.Player;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import dev.prism.api.ui.Title;
import net.rageevent.hypixel.game.events.PlayerDataUpdateEvent;
import net.rageevent.hypixel.game.ui.PlayerDesign;
import net.rageevent.hypixel.player.UserData;
import net.rageevent.hypixel.player.enums.PlayerLevel;

public class ExperienceHandler {

    public void handle(Player player) {
        UserData userData = player.pitData();
        if (userData.level() == 120) return;

        Text oldLevelText = PlayerDesign.formattedLevel(userData);

        int safety = 0;
        boolean leveledUp = false;
        while (userData.level() < 120 && safety < 120) {
            safety++;

            int levelXp = userData.levelExperience();

            if (levelXp < 0 || userData.experience() < levelXp)
                break;

            userData = userData.experience(userData.experience() - levelXp)
                    .level(userData.level() + 1);
            leveledUp = true;
            if (userData.level() == 120) {
                userData = userData.experience(PlayerLevel.LEVEL_120.xp());
                player.savePitData(userData);
                break;
            }
        }
        if (leveledUp) {
            Text currentLevelText = PlayerDesign.formattedLevel(userData);
            player.showTitle(Title.of(Text.of("LEVEL UP!", TextColor.AQUA).bold(),
                    oldLevelText.appendSpace().append(Text.of("➟", TextColor.WHITE))
                            .appendSpace()
                            .append(currentLevelText)
            ));
            player.instance().events().post(PlayerDataUpdateEvent.of(player, PlayerDataUpdateEvent.StatType.LEVEL,userData.level()));
        }
        player.savePitData(userData);
    }

}
