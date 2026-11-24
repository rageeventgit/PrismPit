package net.rageevent.hypixel.game.instance;

import dev.datum.GameMap;
import dev.datum.Region;
import dev.prism.api.entity.Player;
import dev.prism.api.inventory.PlayerInventory;
import dev.prism.api.math.Pos;
import dev.prism.api.plugin.PluginContext;
import dev.prism.api.world.Instance;
import net.rageevent.hypixel.game.GameSettings;
import net.rageevent.hypixel.game.events.PlayerRegionEvent;
import net.rageevent.hypixel.player.UserData;
import net.rageevent.hypixel.player.data.InventoryBytes;
import net.rageevent.hypixel.player.data.PlayerTracker;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GameSchedule {

    private final UserRegionStore regionStore = new UserRegionStore();

    public GameSchedule(GameMap mapData, PluginContext context, Instance instance, PlayerTracker playerTracker) {
        instance.scheduler().every(GameSettings.REGION_CHECK_RATE, task -> {
            if (mapData == null || !instance.isLoaded()) {
                task.cancel(); //Make sure it's shutdown.
                return;
            }
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
                    } else if (!isInRegion && regionStore.isUserStoredInRegion(playerId, region)) {
                        instance.events().post(new PlayerRegionEvent(player, PlayerRegionEvent.Status.LEFT, region.id()));
                        regionStore.removeFromRegion(playerId);
                    }
                }
            }
        });
        instance.scheduler().every(GameSettings.SAVE_PLAYER_DATA_RATE, task -> {
            if (mapData == null || !instance.isLoaded()) {
                task.cancel(); //Make sure it's shutdown.
                return;
            }
            for (Player player : instance.players()) {
                PlayerInventory inventory = player.inventory();
                UserData userData = playerTracker.getOrCreate(player);
                playerTracker.put(player.uuid(),
                        userData.inventory(InventoryBytes.serialize(inventory))
                                .enderChest(InventoryBytes.serialize(player.enderChest()))
                );
                playerTracker.update(player);
            }
        });
    }


    public static class UserRegionStore {
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
