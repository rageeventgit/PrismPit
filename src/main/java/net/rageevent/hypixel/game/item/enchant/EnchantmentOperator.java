package net.rageevent.hypixel.game.item.enchant;

@FunctionalInterface
public interface EnchantmentOperator<T, U, R> {
    R apply(T source, U target, UsableEnchant data);
}
