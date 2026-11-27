package net.rageevent.hypixel.game.item.enchant;

import dev.prism.api.item.ItemType;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.rageevent.hypixel.game.RewardResolver;
import net.rageevent.hypixel.game.combat.DamageAddon;
import net.rageevent.hypixel.game.combat.OperationType;
import net.rageevent.hypixel.game.item.enchant.enums.ApplicableType;
import net.rageevent.hypixel.game.item.enchant.enums.EnchantCategory;
import net.rageevent.hypixel.game.item.enchant.provider.EnchantProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
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

    COLOSSAL_XP(EnchantCategory.RARE, "Colossal XP", _ -> "Gain <yellow>lots</yellow> of <aqua>XP</aqua> on kill",
            5, List.of(ApplicableType.SWORD, ApplicableType.BOW), EnchantProvider.of().xp((source, _, enchant) ->
            RewardResolver.Modifier.multiplier(enchant.level() * 56))),

    RANSOM(EnchantCategory.CORRUPTED, "Ransom", _ -> "A <green>5%</green> to take <yellow>half</yellow> <aqua>XP</aqua> from your <red>killer</red> on death.",
            3, List.of(ApplicableType.PANT), EnchantProvider.of().killed((source, _, data) -> {
        if (ThreadLocalRandom.current().nextInt(0, 100) < 6)
            source.sendStylizedMessage("RANSOM", TextColor.DARK_RED,
                    source.displayName().color(data.target().rank().color()).color(TextColor.GRAY).appendSpace().append(Text.parse("Took <yellow>half</yellow> of your <aqua>XP</aqua>!")));
        //TODO: Gotta add a way of having both, target & source xp & gold rewards
    })),
    ;

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

    static {
        for (ItemEnchant value : values()) {
            BY_ID.put(value.name(), value);
        }
    }

    public boolean applicable(ItemType itemType) {
        return applicableTypes.stream().anyMatch(applicableType -> applicableType.isApplicable(itemType));
    }
}
