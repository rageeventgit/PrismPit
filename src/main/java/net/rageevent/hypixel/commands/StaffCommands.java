package net.rageevent.hypixel.commands;


import dev.datum.Datum;
import dev.prism.api.command.*;
import dev.prism.api.entity.Player;
import dev.prism.api.math.Pos;
import dev.prism.api.plugin.PluginContext;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import dev.prism.api.world.Instance;
import dev.prism.api.world.InstanceSpec;
import dev.prism.api.world.WorldTemplate;
import net.rageevent.fern.FernClient;
import net.rageevent.hypixel.player.UserData;
import net.rageevent.hypixel.player.enums.PlayerRank;

import java.time.Duration;

public class StaffCommands {

    public StaffCommands(PluginContext context, FernClient fernClient, Datum datum) {
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

                        executor.reply("<green>Success!");
                    } catch (IllegalArgumentException e) {
                        throw executor.fail(e.getMessage());
                    }
                }).
                build());

        Argument<String> mapName = Args.greedy("mapName");
        context.commands().register(Command.create("loadmap").argument(mapName).executes(executor -> {
                    if (!executor.isPlayer()) {
                        throw executor.fail("You must be a player to use this");
                    }

                    Player player = executor.player();
                    if (player.rank() != PlayerRank.ADMIN) {
                        throw executor.fail("You must be admin to perform this command.");
                    }

                    executor.reply("Downloading map.");
                    fernClient.downloadMap(executor.get(mapName), context.dataFolder().resolve("maps/")).whenComplete(((path, throwable) -> {
                        if (throwable != null) {
                            executor.fail(throwable.getLocalizedMessage());
                            return;
                        }
                        executor.reply("<green>Successfully downloaded map.");
                        executor.reply("<yellow>Creating world template.");
                        context.server().worlds().loadQuartz(path).whenComplete(((worldTemplate, templateError) -> {
                            if (templateError != null) {
                                executor.fail(templateError.getLocalizedMessage());
                                return;
                            }
                            executor.reply("<light_purple>Loaded quartz world.");
                            executor.reply("<gold>Creating instance.");
                            Instance mapInstance = context.server().instances().create(InstanceSpec.from(worldTemplate));
                            executor.reply("<gray>Teleporting to instance.");

                            player.setInstance(mapInstance, worldTemplate.spawn()).whenComplete(((_, _) -> {
                                executor.reply("Successfully made instance");
                            }));
                            context.scheduler().later(Duration.ofMinutes(2), () -> mapInstance.unload(context.server().instances().defaultInstance()));
                        }));
                    }));
                }).
                build());
    }
}
