package net.rageevent.hypixel.game.combat;

import dev.prism.api.entity.Player;
import dev.prism.api.inventory.PlayerInventory;
import dev.prism.api.item.Item;
import dev.prism.api.item.ItemType;

import java.util.List;

public class DamageCalculator {

    private static final int[] ARMOR_SLOTS = new int[]{36, 37, 38, 39};


    public float calculate(Player damaged, List<DamageAddon> addons, float base) {
        double piercing = 0, flat = 0, coefficient = 1;
        double percentages = 0;

        for (DamageAddon addon : addons) {
            if (addon.damageType() == DamageType.PIERCING) {
                piercing += addon.value();
                continue;
            }
            //noinspection SwitchStatementWithTooFewBranches
            switch (addon.operationType()) {
                case FLAT -> flat += addon.value();
                case MULTIPLIER -> coefficient *= addon.value();
            }
        }

        double reduction = armorReduction(damaged.inventory());
        return (float) ((((double) base + flat) * coefficient * (1 - (reduction * 0.04))) + piercing);
    }

    private double armorReduction(PlayerInventory inventory) {
        double reduction = 0d;

        for (int armorSlot : ARMOR_SLOTS) {
            Item item = inventory.get(armorSlot);
            ItemType type = item.type();

            if (type.isAir()) continue;
            String typeKey = type.key().value();

            String[] parts = typeKey.toUpperCase().split("_");
            TypeTier typeTier = TypeTier.valueOf(parts[0]);
            ArmorType armorType = ArmorType.valueOf(parts[1]);
            reduction += armorType.base() + typeTier.additive().apply(type);
        }

        return reduction;
    }
}
