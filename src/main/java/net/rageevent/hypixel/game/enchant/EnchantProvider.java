package net.rageevent.hypixel.game.enchant;

import dev.prism.api.entity.Player;
import lombok.Data;
import lombok.experimental.Accessors;
import net.rageevent.hypixel.game.combat.DamageAddon;

import java.util.function.Consumer;

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
    private EnchantmentOperator<Player, Player, DamageAddon> hit;
    private EnchantmentOperator<Player, Player, Double> gold;
    private EnchantmentOperator<Player, Player, Integer> xp;
    private EnchantmentOperator<Player,Player, Consumer<Runnable>> opposingTask; //This will have to be checked with armor and all of that, or swords.
    private BiEnchantmentOperator<Player,Player> killed;
    private ItemEnchant[] disables;

    public static EnchantProvider of(){
        return new EnchantProvider();
    }
}
