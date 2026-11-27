package net.rageevent.hypixel.game.item.enchant;

import java.util.List;

public record UsableEnchant(int level, List<ItemEnchant> opposingEnchants) {
}
