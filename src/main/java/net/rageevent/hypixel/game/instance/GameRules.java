package net.rageevent.hypixel.game.instance;

import dev.datum.GameMap;
import dev.datum.Region;
import dev.prism.api.entity.Entity;
import dev.prism.api.entity.Player;
import dev.prism.api.event.Subscription;
import dev.prism.api.event.entity.EntityDamageEvent;
import dev.prism.api.event.player.PlayerChatEvent;
import dev.prism.api.math.Pos;
import dev.prism.api.math.Vec;
import dev.prism.api.plugin.PluginContext;
import dev.prism.api.scheduler.Task;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import dev.prism.api.world.Instance;
import net.rageevent.hypixel.game.events.PlayerRegionEvent;
import net.rageevent.hypixel.player.UserData;
import net.rageevent.hypixel.player.data.PlayerTracker;
import net.rageevent.hypixel.player.enums.PlayerPrestige;
import net.rageevent.hypixel.player.enums.PlayerRank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GameRules {

    private final List<Subscription> subscriptions = new ArrayList<>();
    private final UserRegionStore regionStore = new UserRegionStore();

    public GameRules(GameMap mapData, Instance instance, PlayerTracker playerTracker) {
        subscriptions.add(instance.events().on(PlayerChatEvent.class, event -> event.setFormat((player, message) -> {
            UserData userData = playerTracker.getOrCreate(player);
            PlayerRank playerRank = userData.rank();

            PlayerPrestige playerPrestige = userData.prestige();
            int level = userData.level();

            Text levelText = Text.of("[", playerPrestige.bracketColor())
                    .append(Text.of(String.valueOf(level), userData.levelInfo().color()))
                    .append(Text.of("]", playerPrestige.bracketColor()));

            //Could probably store this somehow, I'm too lazy though.
            if (playerPrestige != PlayerPrestige.P0) {
                levelText = Text.of("[", playerPrestige.bracketColor())
                        .append(Text.of(playerPrestige.name(), TextColor.YELLOW))
                        .append(Text.of("-", playerPrestige.bracketColor()))
                        .append(Text.of(String.valueOf(level), userData.levelInfo().color()))
                        .append(Text.of("]", playerPrestige.bracketColor()));
            }
            levelText = levelText.appendSpace();

            Text messageText = Text.join(Text.of(": "), playerRank == PlayerRank.ADMIN ? Text.parse(message) : Text.of(message)).color(TextColor.WHITE);
            if (playerRank == PlayerRank.NORMAL) {
                messageText = messageText.color(TextColor.GRAY);
            }
            return Text.join(levelText, playerRank.prefix(), player.displayName()).color(playerRank.color()).append(messageText);
        })));
        subscriptions.add(instance.events().on(EntityDamageEvent.class, event -> {
            Entity entity = event.entity();
            Pos entityPosition = entity.position();
            if (!mapData.isIn("spawn_region", entityPosition.x(), entityPosition.y(), entityPosition.z())) return;
            event.setCancelled(true);
        }));

        subscriptions.add(instance.events().on(PlayerRegionEvent.class, event -> {
            Player player = event.player();
            PlayerRegionEvent.Status status = event.status();
            String regionName = event.regionName();

            if (status == PlayerRegionEvent.Status.ENTERED)
                player.sendStylizedMessage("REGIONS", TextColor.DARK_AQUA, "<yellow>You entered <gold>${regionName}</gold>!");
            else
                player.sendStylizedMessage("REGIONS", TextColor.DARK_AQUA, "<red>You left <gold>${regionName}</gold>!");
        }));


        instance.scheduler().every(4, task -> {
            if (!instance.isLoaded()) task.cancel(); //Make sure it's shutdown.
            List<Region> regions = mapData.regions();
            for (Player player : instance.players()) {
                Pos playerPosition = player.position();
                UUID playerId = player.uuid();

                for (Region region : regions) {
                    if (region.type().equalsIgnoreCase("spawn_region")) continue;
                    boolean isInRegion = region.contains(playerPosition.x(), playerPosition.y(), playerPosition.z());

                    if (isInRegion && !regionStore.isUserStoredInRegion(playerId, region)) {
                        instance.events().post(new PlayerRegionEvent(player, PlayerRegionEvent.Status.ENTERED, region.id()));
                        regionStore.addToRegion(playerId, region);
                    } else {
                        instance.events().post(new PlayerRegionEvent(player, PlayerRegionEvent.Status.LEFT, region.id()));
                        regionStore.removeFromRegion(playerId);
                    }
                }

            }
        });
    }

    public void stop() {
        subscriptions.forEach(Subscription::unsubscribe);
    }

    private class UserRegionStore {
        private final ConcurrentMap<UUID, String> usersInRegions = new ConcurrentHashMap<>();

        public void addToRegion(UUID player, Region region) {
            usersInRegions.putIfAbsent(player, region.id());
        }

        public void removeFromRegion(UUID player) {
            usersInRegions.remove(player);
        }

        public boolean isUserStoredInRegion(UUID player, Region region) {
            String regionId = usersInRegions.get(player);
            if (regionId == null)
                return false;
            return regionId.equals(region.id());
        }

        public void clear() {
            usersInRegions.clear();
        }
    }
}
