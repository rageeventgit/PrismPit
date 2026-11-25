package net.rageevent.hypixel.game.combat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public enum ArmorType {

    HELMET(2),
    CHESTPLATE(5),
    LEGGINGS(4),
    BOOTS(1),
    ;
    private final int base;
}
