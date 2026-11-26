package net.rageevent.hypixel.game.rule;

import dev.datum.GameMap;
import dev.prism.api.combat.MeleeHit;
import dev.prism.api.entity.Entity;
import dev.prism.api.entity.GameMode;
import dev.prism.api.entity.Player;
import dev.prism.api.entity.Projectile;
import dev.prism.api.event.Subscription;
import dev.prism.api.event.entity.EntityDamageEvent;
import dev.prism.api.event.player.PlayerBlockBreakEvent;
import dev.prism.api.event.player.PlayerBlockPlaceEvent;
import dev.prism.api.event.player.PlayerChatEvent;
import dev.prism.api.event.player.PlayerInstanceChangeEvent;
import dev.prism.api.math.Pos;
import dev.prism.api.plugin.PluginContext;
import dev.prism.api.scoreboard.Teams;
import dev.prism.api.sound.Sound;
import dev.prism.api.sound.Sounds;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import dev.prism.api.ui.Title;
import dev.prism.api.world.Instance;
import net.rageevent.hypixel.game.GameSettings;
import net.rageevent.hypixel.game.combat.DamageAddon;
import net.rageevent.hypixel.game.combat.DamageCalculator;
import net.rageevent.hypixel.game.enchant.ItemEnchant;
import net.rageevent.hypixel.game.enchant.ItemEnchantHandler;
import net.rageevent.hypixel.game.enchant.UsableEnchant;
import net.rageevent.hypixel.game.events.PlayerDataUpdateEvent;
import net.rageevent.hypixel.game.events.PlayerRegionEvent;
import net.rageevent.hypixel.game.instance.GameInstance;
import net.rageevent.hypixel.game.instance.GameSchedule;
import net.rageevent.hypixel.game.item.StoredEnchant;
import net.rageevent.hypixel.game.mechanic.ExperienceHandler;
import net.rageevent.hypixel.game.ui.GameBoard;
import net.rageevent.hypixel.game.ui.PlayerDesign;
import net.rageevent.hypixel.game.ui.PlayerGameTag;
import net.rageevent.hypixel.player.UserData;
import net.rageevent.hypixel.player.data.InventoryBytes;
import net.rageevent.hypixel.player.data.PlayerTracker;
import net.rageevent.hypixel.player.enums.PlayerPrestige;
import net.rageevent.hypixel.player.enums.PlayerRank;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class GameRules {

    private final List<Subscription> subscriptions = new ArrayList<>();
    private final GameSchedule.UserRegionStore regionStore = new GameSchedule.UserRegionStore();

    public GameRules(GameMap mapData, PluginContext context, GameInstance gameInstance, Instance instance, PlayerTracker playerTracker) {
        GameBoard gameBoard = new GameBoard(playerTracker);

        Teams teams = context.server().scoreboards().createTeams()
                .refresh(GameSettings.NAMETAG_UPDATE_RATE);

        subscriptions.add(instance.events().on(PlayerChatEvent.class, event -> event.setFormat((player, message) -> {
            UserData userData = playerTracker.getOrCreate(player);
            PlayerRank playerRank = userData.rank();

            PlayerPrestige playerPrestige = userData.prestige();

            Text innerText;
            if (playerPrestige != PlayerPrestige.P0) {
                innerText = Text.of(playerPrestige.name(), TextColor.YELLOW)
                        .append(Text.of("-", playerPrestige.bracketColor()))
                        .append(userData.levelText());
            } else {
                innerText = userData.levelText();
            }

            Text levelText = Text.of("[", playerPrestige.bracketColor())
                    .append(innerText)
                    .append(Text.of("]", playerPrestige.bracketColor()));

            levelText = levelText.appendSpace();

            Text messageText = Text.join(Text.of(": "), playerRank == PlayerRank.ADMIN ? Text.parse(message) : Text.of(message)).color(TextColor.WHITE);
            if (playerRank == PlayerRank.NORMAL) {
                messageText = messageText.color(TextColor.GRAY);
            }
            return Text.join(levelText, playerRank.prefix(), player.displayName()).color(playerRank.color()).append(messageText);
        })));
        DamageCalculator damageCalculator = new DamageCalculator();

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
            float base = damageInfo.base();

            if (damageInfo.critical())
                base *= 1.5f;

            base += damageInfo.enchantments();

            List<DamageAddon> damageAddons = new ArrayList<>();
            List<StoredEnchant> enchants = ItemEnchantHandler.fromInventory(attacker.inventory());
            Player finalAttacker = attacker;

            for (StoredEnchant enchant : enchants) {
                ItemEnchant.fromStored(enchant).map(ItemEnchant::provider).ifPresent(provider -> {
                    if (provider.hit() != null)
                        damageAddons.add(provider.hit().apply(finalAttacker, player, new UsableEnchant(enchant.level(), List.of())));
                });
            }

            float calculate = damageCalculator.calculate(player, damageAddons, base);
            event.setAmount(calculate);
            attacker.sendStylizedMessage("DAMAGE", TextColor.DARK_GREEN, "<red>$calculate</red>");

            if (player.health() - calculate <= 0) {
                event.setCancelled(true);
                player.setHealth(20);

                AtomicInteger givenXp = new AtomicInteger(5 + ThreadLocalRandom.current().nextInt(5, 10));
                AtomicReference<Double> givenGold = new AtomicReference<>(ThreadLocalRandom.current().nextDouble(5, 25));

                for (StoredEnchant enchant : enchants) {
                    ItemEnchant.fromStored(enchant).map(ItemEnchant::provider).ifPresent(provider -> {
                        UsableEnchant usableEnchant = new UsableEnchant(enchant.level(), List.of());
                        if (provider.gold() != null)
                            givenGold.updateAndGet(v -> v + provider.gold().apply(finalAttacker, player, usableEnchant));
                        if (provider.xp() != null)
                            givenXp.addAndGet(provider.xp().apply(finalAttacker, player, usableEnchant));
                        if (provider.killed() != null)
                            provider.killed().apply(finalAttacker, player, usableEnchant);
                    });
                }
                attacker.instance().events().post(PlayerDataUpdateEvent.of(attacker, PlayerDataUpdateEvent.StatType.EXPERIENCE, givenXp.get()));

                player.sendStylizedMessage("DEATH", TextColor.RED,
                        Text.of("Killed by ", TextColor.GRAY)
                                .append(PlayerDesign.formattedLevel(attacker.pitData()))
                                .appendSpace()
                                .append(player.displayName().color(player.rank().color()))
                                .append(Text.of(".", TextColor.GRAY))
                );
                attacker.sendStylizedMessage("KILL", TextColor.GREEN,
                        PlayerDesign.formattedLevel(player.pitData())
                                .append(player.displayName().color(player.rank().color()))
                                .appendSpace()
                                .append(Text.of("+${givenXp.get()}XP", TextColor.AQUA).appendSpace())
                                .append(Text.of("+${givenGold.get()}g", TextColor.GOLD))
                );
                attacker.sendActionBar(player.displayName().color(player.rank().color())
                        .appendSpace().append(Text.of("KILL!", TextColor.GREEN).bold()));

                player.showTitle(Title.of(Text.EMPTY, Text.of("YOU DIED!", TextColor.RED).bold()));
                player.playSound(Sound.of(Sounds.BLOCK_ANVIL_PLACE).pitch(2));
                player.teleport(gameInstance.spawn());
            }
        }));
        subscriptions.add(instance.events().on(PlayerRegionEvent.class, event -> {
            Player player = event.player();
            PlayerRegionEvent.Status status = event.status();
            String regionName = event.regionName();
            if (status == PlayerRegionEvent.Status.ENTERED)
                player.sendStylizedMessage("REGIONS", TextColor.DARK_AQUA, "<yellow>You entered <gold>${regionName}</gold>!</yellow>");
            else
                player.sendStylizedMessage("REGIONS", TextColor.DARK_AQUA, "<red>You left <gold>${regionName}</gold>!</red>");
        }));
        subscriptions.add(instance.events().on(PlayerInstanceChangeEvent.class, event -> {
            Player player = event.player();
            UserData userData = player.pitData();

            if (event.from() != null && !event.from().name().equalsIgnoreCase(instance.name())) {
                player.showBoard(null);
                player.savePitData(userData.inventory(InventoryBytes.serialize(player.inventory())).enderChest(InventoryBytes.serialize(player.enderChest())));
                PlayerGameTag.removeTeam(player, teams);
                player.inventory().clear();
                return;
            }
            if (!event.to().name().equalsIgnoreCase(instance.name())) return;
            PlayerGameTag.createTeam(player, teams);
            InventoryBytes.load(userData.inventory(), player.inventory());
            InventoryBytes.load(userData.enderChest(), player.enderChest());

            teams.show(player);
            instance.players().forEach(teams::show);
            gameBoard.show(player);
        }));

        subscriptions.add(instance.events().on(PlayerBlockBreakEvent.class, event -> {
            if (event.player().gameMode() != GameMode.CREATIVE) event.setCancelled(true);
        }));
        subscriptions.add(instance.events().on(PlayerBlockPlaceEvent.class, event -> {
            if (event.player().gameMode() != GameMode.CREATIVE) event.setCancelled(true);
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
        regionStore.clear();
    }


}
