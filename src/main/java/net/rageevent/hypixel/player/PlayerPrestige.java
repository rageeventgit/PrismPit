package net.rageevent.hypixel.player;

import dev.prism.api.text.TextColor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum PlayerPrestige {

    P0(1,TextColor.GRAY,10000),

    I(1.1,TextColor.BLUE,10000),
    II(1.2,TextColor.BLUE,10000),
    III(1.3,TextColor.BLUE,10000),
    IV(1.4,TextColor.BLUE,10000),

    V(1.5,TextColor.YELLOW,10000),
    VI(1.75,TextColor.YELLOW,10000),
    VII(2,TextColor.YELLOW,10000),
    VIII(2.5,TextColor.YELLOW,10000),
    IX(3,TextColor.YELLOW,10000),


    X(4,TextColor.GOLD,10000),
    XI(5,TextColor.GOLD,10000),
    XII(6,TextColor.GOLD,10000),
    XIII(7,TextColor.GOLD,10000),
    XIV(8,TextColor.GOLD,10000),
    
    XV(9,TextColor.RED,10000),
    XVI(10,TextColor.RED,10000),
    XVII(12,TextColor.RED,10000),
    XVIII(14,TextColor.RED,10000),
    XIX(16,TextColor.RED,10000),

    ;
    private final double multiplier;
    private final TextColor bracketColor;
    private final int requiredGold;

}
