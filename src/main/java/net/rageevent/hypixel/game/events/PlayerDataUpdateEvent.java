package net.rageevent.hypixel.game.events;

import dev.prism.api.entity.Player;
import dev.prism.api.event.player.PlayerEvent;

public record PlayerDataUpdateEvent<T>(Player player, StatType type, T value) implements PlayerEvent {

    public enum StatType{
        LEVEL,
        PRESTIGE,
        EXPERIENCE,
        BOUNTY,
        RANK
    }

    public static PlayerDataUpdateEvent<?> of(Player player, StatType type, Object value){
        return new PlayerDataUpdateEvent<>(player,type,value);
    }

    public <R> R value(Class<R> tClass){
        return tClass.cast(value);
    }
}
