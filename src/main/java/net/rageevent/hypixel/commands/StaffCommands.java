package net.rageevent.hypixel.commands;


import dev.prism.api.combat.CombatSettings;
import dev.prism.api.command.*;
import dev.prism.api.entity.Player;
import dev.prism.api.plugin.PluginContext;
import dev.prism.api.sound.Sound;
import dev.prism.api.sound.Sounds;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import dev.prism.api.world.Instance;
import dev.prism.api.world.InstanceSpec;
import net.rageevent.fern.FernClient;
import net.rageevent.hypixel.game.events.PlayerDataUpdateEvent;
import net.rageevent.hypixel.player.UserData;
import net.rageevent.hypixel.player.enums.PlayerLevel;
import net.rageevent.hypixel.player.enums.PlayerPrestige;
import net.rageevent.hypixel.player.enums.PlayerRank;
import net.rageevent.hypixel.util.RomanNumber;

import java.time.Duration;

public class StaffCommands {

    public StaffCommands(PluginContext context, FernClient fernClient) {
        CommandManager commands = context.commands();
        commands.register(rankCommand(context));
        commands.register(levelCommand(context));
        commands.register(prestigeCommand(context));
        commands.register(experienceCommand(context));

        Argument<String> mapName = Args.greedy("mapName");
        Instance defaultInstance = context.server().instances().defaultInstance();
        commands.register(Command.create("loadmap").argument(mapName).executes(executor -> {
                    if (!executor.isPlayer()) {
                        throw executor.fail("You must be a player to use this");
                    }

                    Player player = executor.player();
                    if (player.rank() != PlayerRank.ADMIN) {
                        throw executor.fail("You must be admin to perform this command.");
                    }

                    executor.reply("Downloading map.");
                    fernClient.downloadMap(executor.get(mapName), context.dataFolder().resolve("customMap")).whenComplete(((path, throwable) -> {
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
                            mapInstance.rules()
                                    .mobSpawning(false)
                                    .combat(CombatSettings.LEGACY_1_8)
                                    .randomTickSpeed(0)
                                    .fallDamage(false)
                                    .fireSpreads(false)
                                    .fluidsFlow(false)
                                    .blockDrops(false)
                                    .pvp(false);

                            context.scheduler().later(Duration.ofMinutes(2), () -> {
                                mapInstance.unload(defaultInstance);
                                executor.reply("<red>Deleting map instance");
                            });
                        }));
                    }));
                }).
                build());
    }

    private Command levelCommand(PluginContext context) {
        Argument<Player> target = Args.player("target").optional();
        Argument<Integer> level = Args.integer("level", 1, 120);

        return Command.create("level", "lvl")
                .argument(level)
                .argument(target)
                .executes(executor -> {
                    if (executor.isPlayer() && executor.player().rank() != PlayerRank.ADMIN)
                        executor.fail("You must be admin or higher to use this");

                    int upgradedLevel = executor.get(level);
                    Player targetPlayer = executor.get(target);

                    if (targetPlayer == null && executor.isPlayer())
                        targetPlayer = executor.player();
                    else if (targetPlayer == null && !executor.isPlayer()) {
                        throw executor.fail("You must supply a player");
                    }

                    UserData userData = targetPlayer.pitData();

                    targetPlayer.savePitData(userData.level(upgradedLevel).experience(0));

                    PlayerLevel playerLevel = userData.levelInfo();
                    targetPlayer.sendStylizedMessage("UPGRADE", TextColor.GOLD,
                            Text.parse("Your <yellow>level</yellow> has been set to ")
                                    .append(Text.of(upgradedLevel + "", playerLevel.color()))
                                    .append(Text.of(".", TextColor.WHITE))
                    );
                    context.server().events().post(PlayerDataUpdateEvent.of(targetPlayer, PlayerDataUpdateEvent.StatType.LEVEL, upgradedLevel));
                    executor.reply("<green>Success");
                })
                .build();
    }

    private Command experienceCommand(PluginContext context) {
        Argument<Player> target = Args.player("target").optional();
        Argument<Integer> experience = Args.integer("experience", 1, 120);

        return Command.create("experience", "xp")
                .sub(Command.create("get").argument(target).executes(executor -> {
                    if (executor.isPlayer() && executor.player().rank() != PlayerRank.ADMIN)
                        executor.fail("You must be admin or higher to use this");
                    Player targetPlayer = executor.get(target);
                    if (targetPlayer == null && executor.isPlayer())
                        targetPlayer = executor.player();
                    else if (targetPlayer == null && !executor.isPlayer()) {
                        throw executor.fail("You must supply a player");
                    }
                    UserData userData = targetPlayer.pitData();
                    executor.reply("<yellow>Needed Experience:</yellow> <aqua>%,d</aqua>".formatted(userData.neededExperience()));
                    executor.reply("<yellow>Level Experience:</yellow> <aqua>%,d</aqua>".formatted(userData.levelExperience()));
                }).build())
                .argument(experience)
                .argument(target)
                .executes(executor -> {
                    if (executor.isPlayer() && executor.player().rank() != PlayerRank.ADMIN)
                        executor.fail("You must be admin or higher to use this");

                    int givenExperience = executor.get(experience);
                    Player targetPlayer = executor.get(target);

                    if (targetPlayer == null && executor.isPlayer())
                        targetPlayer = executor.player();
                    else if (targetPlayer == null && !executor.isPlayer()) {
                        throw executor.fail("You must supply a player");
                    }

                    targetPlayer.playSound(Sound.of(Sounds.ENTITY_ARROW_HIT_PLAYER).pitch(2));
                    targetPlayer.sendStylizedMessage("UPGRADE", TextColor.GOLD, Text.parse("You have been given <aqua>%,d XP</aqua>!".formatted(givenExperience)));

                    context.server().events().post(PlayerDataUpdateEvent.of(targetPlayer, PlayerDataUpdateEvent.StatType.EXPERIENCE, givenExperience));
                    executor.reply("<green>Success");
                })
                .build();
    }

    private Command prestigeCommand(PluginContext context) {
        Argument<Player> target = Args.player("target").optional();
        Argument<Integer> prestige = Args.integer("prestige", 0, 50);


        return Command.create("prestige", "pres")
                .argument(prestige)
                .argument(target)
                .executes(executor -> {
                    if (executor.isPlayer() && executor.player().rank() != PlayerRank.ADMIN)
                        executor.fail("You must be admin or higher to use this");

                    int upgradedPrestige = executor.get(prestige);
                    Player targetPlayer = executor.get(target);

                    if (targetPlayer == null && executor.isPlayer())
                        targetPlayer = executor.player();
                    else if (targetPlayer == null && !executor.isPlayer()) {
                        throw executor.fail("You must supply a player");
                    }

                    UserData userData = targetPlayer.pitData();
                    PlayerPrestige playerPrestige = upgradedPrestige == 0 ? PlayerPrestige.P0 : PlayerPrestige.valueOf(RomanNumber.toRoman(upgradedPrestige));

                    targetPlayer.savePitData(userData.prestige(playerPrestige));
                    targetPlayer.sendStylizedMessage("UPGRADE", TextColor.GOLD,
                            Text.parse("Your <aqua>prestige</aqua> has been set to ")
                                    .append(Text.of(playerPrestige.name(), playerPrestige.bracketColor()))
                                    .append(Text.of(".", TextColor.WHITE))
                    );
                    context.server().events().post(PlayerDataUpdateEvent.of(targetPlayer, PlayerDataUpdateEvent.StatType.PRESTIGE, playerPrestige));
                    executor.reply("<green>Success");
                })
                .build();
    }

    private Command rankCommand(PluginContext context) {
        Argument<String> rankName = Args.string("rankName");
        Argument<Player> target = Args.player("target");

        return Command.create("rank").argument(target).argument(rankName).executes(executor -> {
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

                        player.savePitData(userData.rank(playerRank));
                        player.sendMessage(Text.of("You are now ").color(TextColor.GREEN)
                                .append(Text.of(playerRank.name(), playerRank.color()))
                                .append(Text.of(".", TextColor.GREEN)));

                        executor.reply("<green>Success!");
                        context.server().events().post(PlayerDataUpdateEvent.of(player, PlayerDataUpdateEvent.StatType.RANK, playerRank));
                    } catch (IllegalArgumentException e) {
                        throw executor.fail(e.getMessage());
                    }
                }).
                build();
    }
}
