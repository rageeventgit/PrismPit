package net.rageevent.hypixel;

import dev.prism.api.Prism;
import dev.prism.api.command.Args;
import dev.prism.api.command.Argument;
import dev.prism.api.command.Command;
import dev.prism.api.command.CommandSender;
import dev.prism.api.entity.GameMode;
import dev.prism.api.entity.Player;
import dev.prism.api.event.player.PlayerChatEvent;
import dev.prism.api.event.player.PlayerConfigureEvent;
import dev.prism.api.event.player.PlayerJoinEvent;
import dev.prism.api.event.player.PlayerQuitEvent;
import dev.prism.api.plugin.Plugin;
import dev.prism.api.plugin.PluginContext;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import net.rageevent.hypixel.player.PlayerExtensions;
import net.rageevent.hypixel.player.UserData;
import net.rageevent.hypixel.player.data.PlayerTracker;
import net.rageevent.hypixel.player.enums.PlayerRank;

public final class PitPlugin implements Plugin {

    private PlayerTracker playerTracker;

    @Override
    public void enable(PluginContext context) {
        this.playerTracker = new PlayerTracker(context.dataFolder());
        PlayerExtensions.setTracker(playerTracker);

        context.events().on(PlayerConfigureEvent.class, event -> {
            Player player = event.player();
            player.setGameMode(GameMode.SURVIVAL);
            player.pitData();
        });

        context.events().on(PlayerJoinEvent.class, event -> {
            event.setJoinMessage(null);
        });

        context.events().on(PlayerQuitEvent.class, event -> {
            event.setQuitMessage(null);
            event.player().removePitData();
        });

        context.events().on(PlayerChatEvent.class, event -> event.setFormat((player, message) -> {
            PlayerRank playerRank = playerTracker.getOrCreate(player).rank();
            return playerRank.prefix().append(player.displayName().color(playerRank.color())).append(Text.of(": ")).color(TextColor.WHITE).append(Text.of(message));
        }));

        Argument<String> rankName = Args.string("rankName");
        Argument<Player> target = Args.player("target");
        context.commands().register(Command.create("rank").argument(target).argument(rankName).executes(executor -> {
                    CommandSender sender = executor.sender();

                    if (executor.isPlayer()) {
                        Player player = (Player) sender;
                        if (player.rank() != PlayerRank.ADMIN) {
                            throw executor.fail("You must be admin to perform this command.");
                        }
                    }
                    String rankIdString = executor.get(rankName);
                    Player player = executor.get(target);

                    if (player == null) {
                        throw executor.fail("Couldn't find that user.");
                    }
                    try {
                        PlayerRank playerRank = PlayerRank.valueOf(rankIdString.toUpperCase());

                        UserData userData = player.pitData();
                        userData.rank(playerRank);

                        player.savePitData(userData);
                        player.sendMessage(Text.of("You are now ").color(TextColor.GREEN)
                                .append(Text.of(playerRank.name(), playerRank.color()))
                                .append(Text.of(".", TextColor.GREEN)));

                    } catch (IllegalArgumentException e) {
                        throw executor.fail(e.getMessage());
                    }
                }).

                build());
    }

    @Override
    public void disable() {
        Prism.server().players().forEach(player -> playerTracker.remove(player));
        playerTracker.flushData();
    }
}
