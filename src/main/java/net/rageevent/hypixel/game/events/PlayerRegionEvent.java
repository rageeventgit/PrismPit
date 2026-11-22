package net.rageevent.hypixel.game.events;

import dev.prism.api.entity.Player;
import dev.prism.api.event.player.PlayerEvent;

public record PlayerRegionEvent(Player player, Status status, String regionName) implements PlayerEvent {

    public enum Status {
        LEFT,
        ENTERED
    }
}
