package net.rageevent.hypixel.player;

import dev.lotus.Extension;
import dev.lotus.This;
import dev.prism.api.entity.Player;
import dev.prism.api.text.JoinConfig;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import net.rageevent.hypixel.player.data.PlayerTracker;
import net.rageevent.hypixel.player.enums.PlayerRank;

@Extension(Player.class)
public class PlayerExtensions {

    private static PlayerTracker playerTracker;

    public static void setTracker(PlayerTracker playerTracker) {
        PlayerExtensions.playerTracker = playerTracker;
    }

    public static UserData pitData(@This Player player) {
        return playerTracker.getOrCreate(player);
    }

    public static void savePitData(@This Player player, UserData userData) {
        playerTracker.put(player.uuid(), userData);
    }

    public static void removePitData(@This Player player) {
        playerTracker.remove(player.uuid());
    }

    public static PlayerRank rank(@This Player player) {
        return playerTracker.getOrCreate(player).rank();
    }

    public static void sendStylizedMessage(@This Player player, String header, TextColor headColor, String markup) {
        sendStylizedMessage(player, header, headColor, Text.parse(markup));
    }

    public static void sendStylizedMessage(@This Player player, String header, TextColor headColor, Text bodyText) {
        Text stylizedSubject = Text.of(header.toUpperCase(), headColor).bold();
        player.sendMessage(Text.join(JoinConfig.spaces(), stylizedSubject, bodyText.colorIfAbsent(TextColor.GRAY)));
    }
}
