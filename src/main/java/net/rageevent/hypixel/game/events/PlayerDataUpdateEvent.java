package net.rageevent.hypixel.game.events;

import dev.prism.api.entity.Player;
import dev.prism.api.event.instance.InstanceEvent;
import dev.prism.api.event.player.PlayerEvent;
import dev.prism.api.world.Instance;

public record PlayerDataUpdateEvent(Player player, Instance instance, StatType type,
                                       Object value) implements InstanceEvent, PlayerEvent {
    public enum StatType {
        LEVEL,
        PRESTIGE,
        EXPERIENCE,
        BOUNTY,
        RANK
    }

    public static PlayerDataUpdateEvent of(Player player, StatType type, Object value) {
        return new PlayerDataUpdateEvent(player, player.instance(), type, value);
    }

    public <T> T typedValue(Class<T> tClass) {
        return tClass.cast(value);
    }
}
