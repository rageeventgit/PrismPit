package net.rageevent.hypixel.game.enchant;

@FunctionalInterface
public interface BiEnchantmentOperator<T, U> {
    void apply(T source, U target, UsableEnchant data);
}
