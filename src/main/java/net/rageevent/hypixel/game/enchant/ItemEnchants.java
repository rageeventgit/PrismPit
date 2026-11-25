package net.rageevent.hypixel.game.enchant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public enum ItemEnchants {



    ;
    private final EnchantCategory category;
    private final String name;
    private final String description;
    private final int maxLevel;
    private final EnchantProvider provider;
}
