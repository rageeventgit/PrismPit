package net.rageevent.hypixel.game.rule;

import dev.datum.GameMap;
import dev.prism.api.combat.MeleeHit;
import dev.prism.api.entity.Entity;
import dev.prism.api.entity.Player;
import dev.prism.api.entity.Projectile;
import dev.prism.api.event.Subscription;
import dev.prism.api.event.entity.EntityDamageEvent;
import dev.prism.api.math.Pos;
import dev.prism.api.scoreboard.Teams;
import dev.prism.api.sound.Sound;
import dev.prism.api.sound.Sounds;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import dev.prism.api.ui.Title;
import dev.prism.api.world.Instance;
import net.rageevent.hypixel.game.RewardResolver;
import net.rageevent.hypixel.game.combat.DamageAddon;
import net.rageevent.hypixel.game.combat.DamageCalculator;
import net.rageevent.hypixel.game.events.PlayerDataUpdateEvent;
import net.rageevent.hypixel.game.instance.GameInstance;
import net.rageevent.hypixel.game.item.StoredEnchant;
import net.rageevent.hypixel.game.item.enchant.ItemEnchantHandler;
import net.rageevent.hypixel.game.item.enchant.data.CombatInfo;
import net.rageevent.hypixel.game.item.enchant.data.UsableEnchant;
import net.rageevent.hypixel.game.item.enchant.provider.EnchantProvider;
import net.rageevent.hypixel.game.mechanic.ExperienceHandler;
import net.rageevent.hypixel.game.ui.PlayerDesign;
import net.rageevent.hypixel.game.ui.PlayerGameTag;
import net.rageevent.hypixel.player.UserData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MechanicRules {
    private final List<Subscription> subscriptions = new ArrayList<>();

    public MechanicRules(GameMap mapData, GameInstance gameInstance, Instance instance, Teams teams) {
        DamageCalculator damageCalculator = new DamageCalculator();
        RewardResolver<Double> goldResolver = new RewardResolver<>(() -> ThreadLocalRandom.current().nextDouble(5, 25));
        RewardResolver<Integer> xpResolver = new RewardResolver<>(() -> ThreadLocalRandom.current().nextInt(5, 15));

        subscriptions.add(instance.events().on(EntityDamageEvent.class, event -> {
            Entity entity = event.entity();
            Pos entityPosition = entity.position();
            if (mapData.isIn("spawn_region", entityPosition.x(), entityPosition.y(), entityPosition.z()) || !(entity instanceof Player player)) {
                event.setCancelled(true);
                return;
            }
            Player attacker = null;

            if (event.source() instanceof Projectile projectile) {
                Entity shooter = projectile.shooter();
                if (shooter instanceof Player shootingPlayer)
                    attacker = shootingPlayer;
            } else if (event.source() instanceof Player source)
                attacker = source;

            if (attacker == null) {
                event.setCancelled(true);
                return;
            }
            MeleeHit damageInfo = event.melee();
            float base = damageInfo.damage();

            List<DamageAddon> damageAddons = new ArrayList<>();
            List<StoredEnchant> attackerEnchants = ItemEnchantHandler.fromInventory(attacker.inventory());
            List<StoredEnchant> playerEnchants = ItemEnchantHandler.fromInventory(player.inventory());

            Player finalAttacker = attacker;
            for (StoredEnchant enchant : attackerEnchants) {
                EnchantProvider provider = enchant.id().provider();
                if (provider.hit() != null)
                    damageAddons.add(provider.hit().apply(finalAttacker, player, new UsableEnchant(enchant.level(), playerEnchants)));
            }
            float calculate = damageCalculator.calculate(player, damageAddons, base);
            event.setAmount(calculate);
            attacker.sendStylizedMessage("DAMAGE", TextColor.DARK_GREEN, "<red>$calculate</red>");

            if (player.health() - calculate <= 0) {
                event.setCancelled(true);
                player.setHealth(20);
                List<RewardResolver.Modifier<Double>> goldModifiers = new ArrayList<>();
                List<RewardResolver.Modifier<Integer>> xpModifiers = new ArrayList<>();

                for (StoredEnchant enchant : attackerEnchants) {
                    EnchantProvider provider = enchant.id().provider();
                    UsableEnchant usableEnchant = new UsableEnchant(enchant.level(), playerEnchants);
                    if (provider.gold() != null)
                        goldModifiers.add(provider.gold().apply(attacker, player, usableEnchant));
                    if (provider.xp() != null)
                        xpModifiers.add(provider.xp().apply(attacker, player, usableEnchant));
                    if (provider.killed() != null)
                        provider.killed().apply(attacker, player, new CombatInfo(enchant.level(), playerEnchants, attacker.pitData(), player.pitData()));
                }

                for (StoredEnchant enchant : playerEnchants) {
                    EnchantProvider provider = enchant.id().provider();
                    UsableEnchant usableEnchant = new UsableEnchant(enchant.level(), attackerEnchants);
                    if (provider.gold() != null)
                        goldModifiers.add(provider.gold().apply(attacker, player, usableEnchant));
                    if (provider.xp() != null)
                        xpModifiers.add(provider.xp().apply(attacker, player, usableEnchant));
                    if (provider.killed() != null)
                        provider.killed().apply(attacker, player, new CombatInfo(enchant.level(), playerEnchants, attacker.pitData(), player.pitData()));
                }

                double givenGold = new BigDecimal(goldResolver.calculate(goldModifiers)).setScale(1, RoundingMode.HALF_UP).doubleValue();
                int givenXp = (int) xpResolver.calculate(xpModifiers);

                attacker.instance().events().post(PlayerDataUpdateEvent.of(attacker, PlayerDataUpdateEvent.StatType.EXPERIENCE, givenXp));
                UserData attackerData = attacker.pitData();

                player.sendStylizedMessage("DEATH", TextColor.RED,
                        Text.of("Killed by ", TextColor.GRAY)
                                .append(PlayerDesign.formattedLevel(attackerData))
                                .appendSpace()
                                .append(attacker.displayName().color(attacker.rank().color()))
                                .appendSpace()
                                .append(Text.of(".", TextColor.GRAY))
                );
                attacker.sendStylizedMessage("KILL", TextColor.GREEN,
                        PlayerDesign.formattedLevel(player.pitData())
                                .appendSpace()
                                .append(player.displayName().color(player.rank().color()))
                                .appendSpace()
                                .append(Text.of("+${givenXp} XP", TextColor.AQUA).appendSpace())
                                .append(Text.of("+${givenGold}g", TextColor.GOLD))
                );
                attacker.savePitData(attackerData.gold(attackerData.gold() + givenGold));

                attacker.sendActionBar(player.displayName().color(player.rank().color())
                        .appendSpace().append(Text.of("KILL!", TextColor.GREEN).bold()));

                player.showTitle(Title.of(Text.of("YOU DIED",TextColor.RED).bold(), Text.EMPTY));
                player.playSound(Sound.of(Sounds.BLOCK_ANVIL_PLACE).pitch(2));
                player.teleport(gameInstance.spawn());
            }
        }));

        ExperienceHandler experienceHandler = new ExperienceHandler();
        subscriptions.add(instance.events().on(PlayerDataUpdateEvent.class, event -> {
            Player player = event.player();
            if (event.type() == PlayerDataUpdateEvent.StatType.BOUNTY) {
                PlayerGameTag.get(player, teams).ifPresent(team -> team.display(PlayerGameTag.format(player.pitData())));
                return;
            }
            if (event.type() == PlayerDataUpdateEvent.StatType.EXPERIENCE) {
                UserData userData = player.pitData();
                int givenExperience = event.typedValue(Integer.class);
                player.savePitData(userData.experience(userData.experience() + givenExperience));
                experienceHandler.handle(player);
                return;
            }
            PlayerGameTag.createTeam(player, teams);
            teams.show(player);
            instance.players().forEach(teams::show);
        }));
    }

    public void stop() {
        subscriptions.forEach(Subscription::unsubscribe);
    }
}
