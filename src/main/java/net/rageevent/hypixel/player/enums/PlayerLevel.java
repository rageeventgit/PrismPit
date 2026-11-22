package net.rageevent.hypixel.player.enums;

import dev.prism.api.text.TextColor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum PlayerLevel {

    LEVEL_0(0,9,15,TextColor.GRAY),
    LEVEL_10(10,19,30,TextColor.BLUE),
    LEVEL_20(20,29,50,TextColor.DARK_AQUA),
    LEVEL_30(30,39,75,TextColor.DARK_GREEN),
    LEVEL_40(40,49,125,TextColor.GREEN),
    LEVEL_50(50,59,300,TextColor.YELLOW),
    LEVEL_60(60,69,600,TextColor.GOLD),
    LEVEL_70(70,79,800,TextColor.RED),
    LEVEL_80(80,89,900,TextColor.DARK_RED),
    LEVEL_90(90,99,1000,TextColor.DARK_PURPLE),
    LEVEL_100(100,109,1200,TextColor.LIGHT_PURPLE),
    LEVEL_110(110,119,1500,TextColor.WHITE),
    LEVEL_120(120,120,-1,TextColor.AQUA),
    ;
    private final int min, max, xp;
    private final TextColor color;
}
