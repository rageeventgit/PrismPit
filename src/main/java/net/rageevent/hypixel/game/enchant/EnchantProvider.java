package net.rageevent.hypixel.game.enchant;

import dev.prism.api.entity.Player;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class EnchantProvider {
    //GOLD_BOOST(NORMAL, "Gold Boost", "Provides more gold", 3, EnchantProvider.hit((damageContext -> player, attacker, and a list of enchants.)), List.of(<canceled enchants>))
    //GOLD_BOOST(NORMAL, "Gold Boost", "Provides more gold", 3, EnchantProvider.disables(ItemEnchant.MIRROR), List.of(<canceled enchants>))

    /*
      Get all items,
      then we get their provider,
      and apply it to the respected fields like xp, gold, and whatever else.
     */
    private EnchantmentOperator<Player, Player, Double> hit;
    private EnchantmentOperator<Player, Player, Double> gold;
    private EnchantmentOperator<Player, Player, Integer> xp;
    private ItemEnchants[] disables;
    private BiEnchantmentOperator<Player,Player> killed;
}
