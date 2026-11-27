package net.rageevent.hypixel.game.item.enchant.provider;

import dev.prism.api.entity.Player;
import lombok.Data;
import lombok.experimental.Accessors;
import net.rageevent.hypixel.game.RewardResolver;
import net.rageevent.hypixel.game.combat.DamageAddon;
import net.rageevent.hypixel.game.item.enchant.ItemEnchant;

@Data
@Accessors(fluent = true)
public class EnchantProvider {
    //GOLD_BOOST(NORMAL, "Gold Boost", "Provides more gold", 3, EnchantProvider.hit((damageContext -> player, source, and a list of enchants.)), List.of(<canceled enchants>))
    //GOLD_BOOST(NORMAL, "Gold Boost", "Provides more gold", 3, EnchantProvider.disables(ItemEnchant.MIRROR), List.of(<canceled enchants>))

    /*
      Get all items,
      then we get their provider,
      and apply it to the respected fields like xp, gold, and whatever else.
     */
    private EnchantmentOperator<Player, Player, DamageAddon> hit;
    private EnchantmentOperator<Player, Player, RewardResolver.Modifier<Double>> gold;
    private EnchantmentOperator<Player, Player, RewardResolver.Modifier<Integer>> xp;
    private BiEnchantmentOperator<Player,Player> killed;
    private ItemEnchant[] disables;

    public static EnchantProvider of(){
        return new EnchantProvider();
    }
}
