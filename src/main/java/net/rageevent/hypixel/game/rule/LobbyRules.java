package net.rageevent.hypixel.game.rule;

import dev.prism.api.combat.CombatSettings;
import dev.prism.api.entity.GameMode;
import dev.prism.api.entity.Player;
import dev.prism.api.event.Subscription;
import dev.prism.api.event.player.*;
import dev.prism.api.plugin.PluginContext;
import dev.prism.api.scoreboard.TabList;
import dev.prism.api.scoreboard.TeamDisplay;
import dev.prism.api.scoreboard.Teams;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import dev.prism.api.world.Instance;
import net.rageevent.hypixel.game.events.PlayerDataUpdateEvent;
import net.rageevent.hypixel.game.ui.PlayerGameTag;
import net.rageevent.hypixel.player.data.PlayerTracker;
import net.rageevent.hypixel.player.enums.PlayerRank;

import java.util.ArrayList;
import java.util.List;

public class LobbyRules {

    private final List<Subscription> subscriptions = new ArrayList<>();

    public LobbyRules(PluginContext context, Instance instance, PlayerTracker playerTracker) {
        instance.rules()
                .mobSpawning(false)
                .combat(CombatSettings.LEGACY_1_8)
                .fallDamage(false)
                .fireSpreads(false)
                .fluidsFlow(false)
                .blockDrops(false)
                .pvp(false);

        Teams teams = context.server().scoreboards().createTeams();
        subscriptions.add(instance.events().on(PlayerConfigureEvent.class, event -> {
            Player player = event.player();
            player.setGameMode(GameMode.SURVIVAL);
            player.pitData();
        }));
        subscriptions.add(instance.events().on(PlayerJoinEvent.class, event -> {
            Player player = event.player();
            PlayerRank playerRank = player.rank();
            event.setJoinMessage(
                    playerRank.prefix().append(player.displayName())
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
        subscriptions.add(instance.events().on(PlayerQuitEvent.class, event -> {
            teams.teamOf(event.player()).ifPresent(team -> teams.remove(team.name()));
            PlayerGameTag.refresh(instance,teams);
        }));
        subscriptions.add(instance.events().on(PlayerInstanceChangeEvent.class, event -> {
            Player player = event.player();
            if(event.from() != null && !event.from().name().equalsIgnoreCase(instance.name())) {
                teams.teamOf(player).ifPresent(team -> teams.remove(team.name()));
                PlayerGameTag.refresh(instance,teams);
            }
            if(event.to().name().equalsIgnoreCase(instance.name())){
                createTeam(teams,instance,player);
            }
        }));
        subscriptions.add(instance.events().on(PlayerDataUpdateEvent.class, event -> {
            if(!event.instance().name().equalsIgnoreCase(instance.name())) return;
            if(event.type() != PlayerDataUpdateEvent.StatType.RANK) return;
            createTeam(teams,instance,event.player());
        }));
    }

    public void stop() {
        subscriptions.forEach(Subscription::unsubscribe);
    }

    private String generateTeamName(Player player){
        return player.rank().ordinal() + player.uuid().toString();
    }
    private void createTeam(Teams teams,Instance instance, Player player){
        teams.teamOf(player).ifPresent(team -> teams.remove(team.name()));
        teams.create(generateTeamName(player))
                .display(this::generateTeamDisplay)
                .add(player);
        PlayerGameTag.refresh(instance,teams);
    }
    private TeamDisplay generateTeamDisplay(Player player){
        PlayerRank playerRank = player.rank();
        return TeamDisplay.of(playerRank.color())
                .prefix(playerRank.prefix());
    }
}
