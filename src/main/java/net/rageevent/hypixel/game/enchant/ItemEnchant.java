package net.rageevent.hypixel.game.enchant;

import dev.prism.api.item.ItemType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.rageevent.hypixel.game.combat.DamageAddon;
import net.rageevent.hypixel.game.combat.OperationType;
import net.rageevent.hypixel.game.item.StoredEnchant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public enum ItemEnchant {
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
        return DamageAddon.normal(OperationType.PERCENT, percentage);
    }))),
    COLOSSAL_XP(EnchantCategory.CORRUPTED, "Colossal XP", _ -> {
        return "Gain the <yellow>max</yellow> amount of <aqua>XP</aqua> on kill";
    }, 1, List.of(ApplicableType.SWORD, ApplicableType.BOW), EnchantProvider.of().xp((_, _, _) -> Integer.MAX_VALUE));

    private final EnchantCategory category;
    private final String displayName;
    private final Function<Integer, String> description;
    private final int maxLevel;
    private final List<ApplicableType> applicableTypes;
    private final EnchantProvider provider;

    private static final Map<String, ItemEnchant> BY_ID = new HashMap<>();

    public static Optional<ItemEnchant> byId(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static Optional<ItemEnchant> fromStored(StoredEnchant enchant) {
        return byId(enchant.enchantId().toUpperCase());
    }

    static {
        for (ItemEnchant value : values()) {
            BY_ID.put(value.name(), value);
        }
    }

    public boolean applicable(ItemType itemType) {
        return applicableTypes.stream().anyMatch(applicableType -> applicableType.isApplicable(itemType));
    }
}
