package net.rageevent.hypixel.game.enchant;

import dev.prism.api.Key;
import dev.prism.api.item.ItemType;

public enum ApplicableType {

    SWORD,
    BOW,
    PANT
    ;

    public boolean isApplicable(ItemType item){
        Key key = item.key();
        if(this == PANT) {
            return key.value().contains("leggings");
        }
        return key.value().contains(name().toLowerCase());
    }
}
