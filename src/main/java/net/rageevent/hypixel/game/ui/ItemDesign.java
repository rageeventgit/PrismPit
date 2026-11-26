package net.rageevent.hypixel.game.ui;

import dev.prism.api.item.Item;
import dev.prism.api.item.ItemComponent;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import lombok.experimental.UtilityClass;
import net.rageevent.hypixel.game.enchant.EnchantCategory;
import net.rageevent.hypixel.game.enchant.ItemEnchant;
import net.rageevent.hypixel.game.enchant.ItemEnchantHandler;
import net.rageevent.hypixel.game.item.ItemInfoKeys;
import net.rageevent.hypixel.game.item.ItemMarker;
import net.rageevent.hypixel.game.item.StoredEnchant;
import net.rageevent.hypixel.util.RomanNumber;
import org.apache.commons.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ItemDesign {

    public Item redesign(Item item) {
        int lives = ItemInfoKeys.info(item, ItemInfoKeys.CURRENT_LIVES, -1);
        int maxLives = ItemInfoKeys.info(item, ItemInfoKeys.MAX_LIVES, -1);
        List<StoredEnchant> enchants = ItemEnchantHandler.getAll(item);
        ItemMarker marker = ItemInfoKeys.info(item, ItemInfoKeys.MARKER, ItemMarker.NONE);
        int tier = ItemInfoKeys.info(item, ItemInfoKeys.TIER, 1);
        List<Text> itemLore = new ArrayList<>();

        if (lives != -1 && maxLives != -1 && marker == ItemMarker.NONE)
            itemLore.addFirst(Text.of("Lives: ", TextColor.GRAY)
                    .append(Text.of(lives + "", (lives < maxLives / 2) ? TextColor.RED : TextColor.GREEN))
                    .append(Text.of("/"))
                    .append(Text.of(maxLives + "", TextColor.GREEN))
            );
        if (marker != ItemMarker.NONE)
            itemLore.addFirst(marker.loreValue());

        for (StoredEnchant enchant : enchants) {
            ItemEnchant.fromStored(enchant).ifPresent(itemEnchant -> {
                EnchantCategory enchantCategory = itemEnchant.category();
                Text categoryText = enchantCategory != EnchantCategory.NORMAL ? Text.of(enchantCategory.name() + "!", enchantCategory.loreColor()).bold().appendSpace() : Text.EMPTY;
                Text enchantText = Text.parse(itemEnchant.displayName()).color(TextColor.BLUE);

                int enchantLevel = enchant.level();
                if (enchantLevel > 1)
                    enchantText = enchantText.appendSpace().append(RomanNumber.toRoman(enchantLevel));
                itemLore.add(Text.join(categoryText, enchantText));

                itemLore.add(Text.parse(WordUtils.wrap(itemEnchant.description().apply(enchantLevel), 32,"\n",false))
                        .colorIfAbsent(TextColor.GRAY));
            });
        }
        Text itemName = item.name() == null ? item.get(ItemComponent.ITEM_NAME) : item.name();
        if (tier > 1)
            itemName.appendSpace().append(RomanNumber.toRoman(tier));

        return item.lore(itemLore);
    }
}
