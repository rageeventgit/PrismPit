package net.rageevent.hypixel.game.ui;

import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import lombok.experimental.UtilityClass;
import net.rageevent.hypixel.player.UserData;

@UtilityClass
public class PlayerDesign {

    public Text formattedLevel(UserData userData) {
        TextColor bracketColor = userData.prestige().bracketColor();
        return Text.of("[", bracketColor)
                .append(userData.levelText())
                .append(Text.of("]", bracketColor));
    }

}
