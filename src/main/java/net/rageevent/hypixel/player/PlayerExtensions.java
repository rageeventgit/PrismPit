package net.rageevent.hypixel.player;

import dev.lotus.Extension;
import dev.lotus.This;
import dev.prism.api.entity.Player;
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
        playerTracker.put(player, userData);
    }
    public static void removePitData(@This Player player){
        playerTracker.remove(player);
    }

    public static PlayerRank rank(@This Player player){
        return pitData(player).rank();
    }
}
