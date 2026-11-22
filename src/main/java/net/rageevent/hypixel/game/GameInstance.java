package net.rageevent.hypixel.game;


import dev.datum.Datum;
import dev.datum.GameMap;
import dev.datum.MapInfo;
import dev.prism.api.combat.CombatSettings;
import dev.prism.api.plugin.PluginContext;
import dev.prism.api.world.Instance;
import dev.prism.api.world.InstanceSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.rageevent.fern.FernClient;
import net.rageevent.hypixel.game.instance.GameRules;
import net.rageevent.hypixel.player.data.PlayerTracker;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class GameInstance {

    private final Datum datum;
    private final FernClient client;
    private final PluginContext context;
    private final PlayerTracker playerTracker;

    private MapInfo chosenMap;
    private Instance mapInstance;
    private boolean mapInitialized;

    private GameRules gameRules;

    @Getter
    private GameMap mapData;

    public void chooseMap() {
        if (mapInitialized)
            throw new RuntimeException("The current map must be removed.");

        List<MapInfo> playableMaps = datum.maps("the-pit").stream().filter(MapInfo::playable).toList();
        int mapIndex = ThreadLocalRandom.current().nextInt(0, playableMaps.size() - 1);
        this.chosenMap = playableMaps.get(mapIndex);
    }

    public void loadMap() {
        if (chosenMap == null)
            throw new RuntimeException("A playable pit map must be chosen");
        client.downloadMap(chosenMap.name(), context.dataFolder().resolve("Pit Map")).whenComplete(((path, throwable) -> {
            if (throwable != null) {
                context.server().console().sendMessage("<red>${throwable.getLocalizedMessage()}");
                return;
            }
            context.server().worlds().loadQuartz(path).whenComplete(((worldTemplate, templateError) -> {
                if (templateError != null) {
                    context.server().console().sendMessage("<red>${templateError.getLocalizedMessage()}");
                    return;
                }
                mapInstance = context.server().instances().create(InstanceSpec.from(worldTemplate));
                mapInstance.rules()
                        .mobSpawning(false)
                        .combat(CombatSettings.LEGACY_1_8)
                        .randomTickSpeed(0)
                        .explosionsBreakBlocks(false)
                        .dragonFight(false)
                        .fallDamage(false)
                        .fireSpreads(false)
                        .fluidsFlow(false)
                        .blockDrops(false)
                        .pvp(true);
                this.gameRules = new GameRules(mapData, mapInstance, playerTracker);
                mapInitialized = true;
            }));
        }));
    }

    public void unloadMap() {
        if (!mapInitialized)
            throw new RuntimeException("A playable map must be loaded.");

        Instance defaultInstance = context.server().instances().defaultInstance();
        mapInstance.players().forEach(player -> {
            player.setInstance(defaultInstance, defaultInstance.spawnPoint());
            player.sendMessage("<gray>Sending you to the lobby");
        });
        gameRules.stop();
        mapInstance.unload(defaultInstance);
        mapInstance = null;
        mapInitialized = false;
        mapData = null;
    }

    public CompletableFuture<Void> grabMapData() {
        if (chosenMap == null)
            throw new RuntimeException("A playable pit map must be chosen");

        return datum.mapAsync(chosenMap.name()).whenComplete(((gameMap, throwable) -> {
            if (throwable != null) {
                context.server().console().sendMessage("<red>${throwable.getLocalizedMessage()}");
                return;
            }
            this.mapData = gameMap;
        })).thenApplyAsync((_) -> null);
    }

}
