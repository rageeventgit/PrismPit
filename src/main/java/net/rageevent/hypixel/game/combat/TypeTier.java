package net.rageevent.hypixel.game.combat;

import dev.prism.api.item.ItemType;
import dev.prism.api.item.Items;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.function.Function;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public enum TypeTier {

    NETHERITE(5),
    DIAMOND(3),
    IRON(1),
    CHAINMAIL(0),
    GOLDEN((material -> material == Items.GOLDEN_BOOTS ? -1 : 0)),
    LEATHER((material -> {
        if (material == Items.LEATHER_BOOTS) return 0;
        if (material == Items.LEATHER_CHESTPLATE) return -2;
        if (material == Items.LEATHER_LEGGINGS) return 1;
        return -1;
    }));
    private final Function<ItemType, Integer> additive;

    TypeTier(int additive) {
        this.additive = (itemType -> additive);
    }

}
