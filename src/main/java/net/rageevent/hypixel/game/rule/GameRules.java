package net.rageevent.hypixel.game.rule;

import dev.datum.GameMap;
import dev.prism.api.entity.GameMode;
import dev.prism.api.entity.Player;
import dev.prism.api.event.Subscription;
import dev.prism.api.event.player.PlayerBlockBreakEvent;
import dev.prism.api.event.player.PlayerBlockPlaceEvent;
import dev.prism.api.event.player.PlayerChatEvent;
import dev.prism.api.event.player.PlayerInstanceChangeEvent;
import dev.prism.api.plugin.PluginContext;
import dev.prism.api.scoreboard.Teams;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import dev.prism.api.world.Instance;
import net.rageevent.hypixel.game.GameSettings;
import net.rageevent.hypixel.game.events.PlayerRegionEvent;
import net.rageevent.hypixel.game.instance.GameInstance;
import net.rageevent.hypixel.game.instance.GameSchedule;
import net.rageevent.hypixel.game.ui.GameBoard;
import net.rageevent.hypixel.game.ui.PlayerGameTag;
import net.rageevent.hypixel.player.UserData;
import net.rageevent.hypixel.player.data.InventoryBytes;
import net.rageevent.hypixel.player.data.PlayerTracker;
import net.rageevent.hypixel.player.enums.PlayerPrestige;
import net.rageevent.hypixel.player.enums.PlayerRank;

import java.util.ArrayList;
import java.util.List;

public class GameRules {

    private final List<Subscription> subscriptions = new ArrayList<>();
    private final GameSchedule.UserRegionStore regionStore = new GameSchedule.UserRegionStore();
    private final MechanicRules mechanicRules;
    public GameRules(GameMap mapData, PluginContext context, GameInstance gameInstance, Instance instance, PlayerTracker playerTracker) {
        GameBoard gameBoard = new GameBoard(playerTracker);
        Teams teams = context.server().scoreboards().createTeams()
                .refresh(GameSettings.NAMETAG_UPDATE_RATE);
        this.mechanicRules = new MechanicRules(mapData,gameInstance,instance,teams);

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
                PlayerGameTag.refresh(instance,teams);
                player.inventory().clear();
                return;
            }
            if (!event.to().name().equalsIgnoreCase(instance.name())) return;
            PlayerGameTag.createTeam(player, teams);
            InventoryBytes.load(userData.inventory(), player.inventory());
            InventoryBytes.load(userData.enderChest(), player.enderChest());
            PlayerGameTag.refresh(instance,teams);
            gameBoard.show(player);
        }));

        subscriptions.add(instance.events().on(PlayerBlockBreakEvent.class, event -> {
            if (event.player().gameMode() != GameMode.CREATIVE) event.setCancelled(true);
        }));
        subscriptions.add(instance.events().on(PlayerBlockPlaceEvent.class, event -> {
            if (event.player().gameMode() != GameMode.CREATIVE) event.setCancelled(true);
        }));

    }

    public void stop() {
        subscriptions.forEach(Subscription::unsubscribe);
        mechanicRules.stop();
        regionStore.clear();
    }


}
