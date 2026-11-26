package net.rageevent.hypixel.game.item;

import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum ItemMarker {

    NONE(Text.EMPTY),
    KEPT_ON_DEATH(Text.of("Kept on death",TextColor.GRAY)),
    ONE_TIME_USE(Text.of("One time use", TextColor.RED))


    ;

    private final Text loreValue;
}
