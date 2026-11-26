package net.rageevent.hypixel.game.item;

import dev.prism.api.item.Item;
import dev.prism.api.nbt.DataKey;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ItemInfoKeys {

    public final DataKey<ItemMarker> MARKER = DataKey.of("marker", ItemMarker.class);
    public final DataKey<List<StoredEnchant>> ENCHANTMENTS = DataKey.list("enchants", StoredEnchant.class);
    public final DataKey<Integer> MAX_LIVES = DataKey.of("maxLives",Integer.class);
    public final DataKey<Integer> TIER = DataKey.of("tier",Integer.class);
    public final DataKey<Integer> CURRENT_LIVES = DataKey.of("lives",Integer.class);

    public final String CONTAINER_KEY = "custom";

    public <T> Item withInfo(Item item, DataKey<T> key, T value){
        return item.editData(data -> data.with(CONTAINER_KEY,
                data.getCompound(CONTAINER_KEY).with(key,value)));
    }
    public <T> T info(Item item, DataKey<T> key, T fallback){
        return item.data().getCompound(CONTAINER_KEY).getOrDefault(key,fallback);
    }
}
