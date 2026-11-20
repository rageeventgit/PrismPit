package net.rageevent.hypixel.game.instance;

import dev.prism.api.entity.GameMode;
import dev.prism.api.entity.Player;
import dev.prism.api.event.Subscription;
import dev.prism.api.event.player.PlayerChatEvent;
import dev.prism.api.event.player.PlayerConfigureEvent;
import dev.prism.api.event.player.PlayerJoinEvent;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import dev.prism.api.world.Instance;
import net.rageevent.hypixel.player.data.PlayerTracker;
import net.rageevent.hypixel.player.enums.PlayerRank;

import java.util.ArrayList;
import java.util.List;

public class LobbyRules {

    private List<Subscription> subscriptions = new ArrayList<>();

    public LobbyRules(Instance instance, PlayerTracker playerTracker) {
        subscriptions.add(instance.events().on(PlayerConfigureEvent.class, event -> {
            Player player = event.player();
            player.setGameMode(GameMode.SURVIVAL);
            player.pitData();
        }));
        subscriptions.add(instance.events().on(PlayerJoinEvent.class, event -> {
            Player player = event.player();
            event.setJoinMessage(
                    player.rank().prefix().append(player.displayName())
                            .appendSpace()
                            .append(Text.of("has joined the lobby!", TextColor.YELLOW)));
        }));
        subscriptions.add(instance.events().on(PlayerChatEvent.class, event -> event.setFormat((player, message) -> {
            PlayerRank playerRank = playerTracker.getOrCreate(player).rank();

            Text messageText = Text.join(Text.of(": "), playerRank == PlayerRank.ADMIN ? Text.parse(message) : Text.of(message)).color(TextColor.WHITE);
            if (playerRank == PlayerRank.NORMAL) {
                messageText = messageText.color(TextColor.GRAY);
            }

            return Text.join(playerRank.prefix(), player.displayName()).color(playerRank.color()).append(messageText);
        })));
    }

    public void stop() {
        subscriptions.forEach(Subscription::unsubscribe);
    }

}
