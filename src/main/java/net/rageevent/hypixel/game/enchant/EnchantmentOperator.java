package net.rageevent.hypixel.game.enchant;

@FunctionalInterface
public interface EnchantmentOperator<T, U, R> {
    R apply(T source, U target, UsableEnchant data);
}
