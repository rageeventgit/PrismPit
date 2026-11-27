package net.rageevent.hypixel.game.item.enchant.data;

import net.rageevent.hypixel.game.item.StoredEnchant;
import net.rageevent.hypixel.player.UserData;

import java.util.List;

public record CombatInfo(int level, List<StoredEnchant> opposing, UserData source, UserData target){
}
