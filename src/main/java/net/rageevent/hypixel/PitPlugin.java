package net.rageevent.hypixel;

import dev.datum.Datum;
import dev.prism.api.Prism;
import dev.prism.api.combat.CombatSettings;
import dev.prism.api.command.CommandSender;
import dev.prism.api.event.player.PlayerQuitEvent;
import dev.prism.api.plugin.Plugin;
import dev.prism.api.plugin.PluginContext;
import dev.prism.api.text.Theme;
import dev.prism.api.world.Instance;
import net.rageevent.fern.FernClient;
import net.rageevent.hypixel.commands.StaffCommands;
import net.rageevent.hypixel.game.instance.LobbyRules;
import net.rageevent.hypixel.player.PlayerExtensions;
import net.rageevent.hypixel.player.data.PlayerTracker;

import java.util.Arrays;

public final class PitPlugin implements Plugin {

    private PlayerTracker playerTracker;
    private Datum datum;
    private FernClient fernClient;


    @Override
    public void enable(PluginContext context) {
        this.playerTracker = new PlayerTracker(context.dataFolder());
        PlayerExtensions.setTracker(playerTracker);

        datum = Datum.http("https://datum.rageevent.net")
                .apiKey(System.getenv("DATUM_API_KEY"))
                .build();

        fernClient = FernClient.builder()
                .url("https://fern.rageevent.net")
                .apiKey(System.getenv("PANEL_API_KEY"))
                .build();

        fernClient.keyInfo().whenComplete((key, error) -> {
            CommandSender console = context.server().console();
            console.sendMessage(Theme.header("Fern"));
            if (error != null)
                console.sendMessage(Theme.error(error.getLocalizedMessage()));
            else {
                console.sendMessage(Theme.field("Name", key.name()));
                console.sendMessage(Theme.field("Scopes", Arrays.toString(key.scopes().toArray())));
            }
        });

        Instance defaultInstance = context.server().instances().defaultInstance();
        new LobbyRules(defaultInstance, playerTracker);

        defaultInstance.rules()
                .mobSpawning(false)
                .combat(CombatSettings.LEGACY_1_8)
                .fallDamage(false)
                .fireSpreads(false)
                .fluidsFlow(false)
                .blockDrops(false)
                .pvp(false);

        context.events().on(PlayerQuitEvent.class, event -> {
            event.setQuitMessage(null);
            event.player().removePitData();
        });

        new StaffCommands(context,fernClient, datum);
    }

    @Override
    public void disable() {
        Prism.server().players().forEach(player -> playerTracker.remove(player.uuid()));
        playerTracker.shutdown();
        datum.close();

        fernClient.close();
    }
}
