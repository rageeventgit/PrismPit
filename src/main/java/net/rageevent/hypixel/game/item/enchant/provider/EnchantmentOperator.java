package net.rageevent.hypixel.game.item.enchant.provider;

import net.rageevent.hypixel.game.item.enchant.data.UsableEnchant;

@FunctionalInterface
public interface EnchantmentOperator<T, U, R> {
    R apply(T source, U target, UsableEnchant data);
}
