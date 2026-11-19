package net.rageevent.hypixel.player.enums;

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

    XX(18,TextColor.DARK_PURPLE,10000),
    XXI(20,TextColor.DARK_PURPLE,10000),
    XXII(24,TextColor.DARK_PURPLE,10000),
    XXIII(28,TextColor.DARK_PURPLE,10000),
    XXIV(32,TextColor.DARK_PURPLE,10000),
    
    XXV(36,TextColor.LIGHT_PURPLE,10000),
    XXVI(40,TextColor.LIGHT_PURPLE,10000),
    XXVII(45,TextColor.LIGHT_PURPLE,10000),
    XXVIII(50,TextColor.LIGHT_PURPLE,10000),
    XXIX(75,TextColor.LIGHT_PURPLE,10000),


    XXX(100,TextColor.WHITE,10000),
    XXXI(101,TextColor.WHITE,10000),
    XXXII(101,TextColor.WHITE,10000),
    XXXIII(101,TextColor.WHITE,10000),
    XXXIV(101,TextColor.WHITE,10000),

    XXXV(101,TextColor.AQUA,10000),
    XXXVI(200,TextColor.AQUA,10000),
    XXXVII(300,TextColor.AQUA,10000),
    XXXVIII(400,TextColor.AQUA,10000),
    XXXIX(500,TextColor.AQUA,10000),

    XL(750,TextColor.DARK_BLUE,10000),
    XLI(1000,TextColor.DARK_BLUE,10000),
    XLII(1250,TextColor.DARK_BLUE,10000),
    XLIII(1500,TextColor.DARK_BLUE,10000),
    XLIV(1750,TextColor.DARK_BLUE,10000),

    XLV(2000,TextColor.DARK_AQUA,10000),
    XLVI(3000,TextColor.DARK_AQUA,10000),
    XLVII(5000,TextColor.DARK_AQUA,10000),
    XLVIII(10000,TextColor.DARK_AQUA,10000),
    XLIX(50000,TextColor.DARK_AQUA,10000),
    L(100000,TextColor.DARK_GRAY,10000),

    ;
    private final double multiplier;
    private final TextColor bracketColor;
    private final int requiredGold;

}
