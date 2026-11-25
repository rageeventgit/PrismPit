package net.rageevent.hypixel.game.enchant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.rageevent.hypixel.game.combat.DamageAddon;
import net.rageevent.hypixel.game.combat.OperationType;

import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public enum ItemEnchants {

    SHARK(EnchantCategory.NORMAL, "Shark", (level) -> {
        int percentage = 2;
        if (level == 2)
            percentage = 4;
        if (level == 3)
            percentage = 7;
        return "Deal <red>+" + percentage + "%</red> damage per player within <yellow>12</yellow> blocks below <red>6♥</red>";
    }, 3, List.of(ApplicableType.SWORD, ApplicableType.BOW), EnchantProvider.of().hit(((source, target, data) -> {
        int level = data.level();
        if (source.position().distanceSquared(target.position()) > 36)
            return DamageAddon.NONE;
        if (target.health() < 12) return DamageAddon.NONE;
        int percentage = 2;
        if (level == 2)
            percentage = 4;
        if (level == 3)
            percentage = 7;
        return DamageAddon.normal(OperationType.PERCENT, (double) percentage / 100);
    })));
    private final EnchantCategory category;
    private final String name;
    private final Function<Integer, String> description;
    private final int maxLevel;
    private final List<ApplicableType> applicableTypes;
    private final EnchantProvider provider;
}
