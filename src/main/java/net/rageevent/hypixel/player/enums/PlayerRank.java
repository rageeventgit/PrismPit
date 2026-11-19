package net.rageevent.hypixel.player.enums;

import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public enum PlayerRank {

    NORMAL(TextColor.GRAY) {
        @Override
        public Text prefix() {
            return Text.of("").color(TextColor.GRAY);
        }
    },
    ADMIN(TextColor.RED);

    private final TextColor color;

    public Text prefix() {
        return Text.of("[${name()}]", color).appendSpace();
    }

}
