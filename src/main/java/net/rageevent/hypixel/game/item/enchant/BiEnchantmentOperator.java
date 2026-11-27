package net.rageevent.hypixel.game.item.enchant;

@FunctionalInterface
public interface BiEnchantmentOperator<T, U> {
    void apply(T source, U target, UsableEnchant data);
}
