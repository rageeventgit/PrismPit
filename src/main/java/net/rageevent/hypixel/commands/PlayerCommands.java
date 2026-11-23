package net.rageevent.hypixel.commands;

import dev.prism.api.command.Command;
import dev.prism.api.command.CommandManager;
import dev.prism.api.entity.Player;
import dev.prism.api.plugin.PluginContext;
import dev.prism.api.world.Instance;
import net.rageevent.hypixel.game.instance.GameInstance;

public class PlayerCommands {

    public PlayerCommands(PluginContext context, GameInstance gameInstance) {
        CommandManager commands = context.commands();
        Instance defaultInstance = context.server().instances().defaultInstance();

        commands.register(Command.create("playpit", "play").executes(executor -> {
            if (!executor.isPlayer())
                throw executor.fail("You must be a player");

            Player player = executor.player();
            player.sendMessage("<yellow>Transferring...");
            gameInstance.transport(player);
        }).build());
        commands.register(Command.create("lobby", "l").executes(executor -> {
            if (!executor.isPlayer())
                throw executor.fail("You must be a player");

            Player player = executor.player();
            player.sendMessage("<gray>Sending you to the lobby.");
            player.setInstance(defaultInstance, defaultInstance.spawnPoint());
        }).build());
    }

}
