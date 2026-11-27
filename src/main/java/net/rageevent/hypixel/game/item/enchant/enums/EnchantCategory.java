package net.rageevent.hypixel.game.item.enchant.enums;

import dev.prism.api.text.TextColor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public enum EnchantCategory {

    NORMAL(TextColor.GRAY),
    RARE(TextColor.LIGHT_PURPLE),
    CORRUPTED(TextColor.hex("#80475E"))
    ;

    private final TextColor loreColor;
}
