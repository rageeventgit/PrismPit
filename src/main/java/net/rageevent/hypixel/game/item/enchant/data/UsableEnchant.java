package net.rageevent.hypixel.game.item.enchant.data;

import net.rageevent.hypixel.game.item.StoredEnchant;

import java.util.List;

public record UsableEnchant(int level, List<StoredEnchant> opposingEnchants) {
}
