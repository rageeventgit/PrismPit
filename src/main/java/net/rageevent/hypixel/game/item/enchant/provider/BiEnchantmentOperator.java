package net.rageevent.hypixel.game.item.enchant.provider;

import net.rageevent.hypixel.game.item.enchant.data.CombatInfo;

@FunctionalInterface
public interface BiEnchantmentOperator<T, U> {
    void apply(T source, U target, CombatInfo data);
}
