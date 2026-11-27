package net.rageevent.hypixel.game.ui;

import dev.prism.api.item.Item;
import dev.prism.api.item.TooltipPart;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import dev.prism.api.text.WrapConfig;
import lombok.experimental.UtilityClass;
import net.rageevent.hypixel.game.item.ItemInfoKeys;
import net.rageevent.hypixel.game.item.ItemMarker;
import net.rageevent.hypixel.game.item.StoredEnchant;
import net.rageevent.hypixel.game.item.enchant.EnchantCategory;
import net.rageevent.hypixel.game.item.enchant.ItemEnchant;
import net.rageevent.hypixel.game.item.enchant.ItemEnchantHandler;
import net.rageevent.hypixel.util.RomanNumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class ItemDesign {

    private final TooltipPart[] TOOLTIP_PARTS = new TooltipPart[TooltipPart.values().length - 1];

    static {
        int partIndex = 0;
        for (TooltipPart value : TooltipPart.values()) {
            if (value == TooltipPart.LORE) continue;
            TOOLTIP_PARTS[partIndex] = value;
            partIndex++;
        }
    }

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
                    .notItalic()
            );

        if (!enchants.isEmpty())
            itemLore.add(Text.EMPTY);
        for (StoredEnchant enchant : enchants) {
            ItemEnchant.fromStored(enchant).ifPresent(itemEnchant -> {
                EnchantCategory enchantCategory = itemEnchant.category();
                Text categoryText = enchantCategory != EnchantCategory.NORMAL ? Text.of(enchantCategory.name() + "!", enchantCategory.loreColor()).bold().appendSpace() : Text.EMPTY;
                Text enchantText = Text.parse(itemEnchant.displayName()).color(TextColor.BLUE);

                int enchantLevel = enchant.level();
                if (enchantLevel > 1)
                    enchantText = enchantText.appendSpace().append(RomanNumber.toRoman(enchantLevel));
                itemLore.add(Text.join(categoryText, enchantText).notItalic());
                itemLore.addAll(Text.parse(itemEnchant.description().apply(enchantLevel))
                        .colorIfAbsent(TextColor.GRAY).notItalic().wrap(WrapConfig.of(30).breakLongWords(false)));
                itemLore.add(Text.EMPTY);
            });
        }

        if (marker != ItemMarker.NONE)
            itemLore.add(marker.loreValue());

        Text itemName = item.displayName();
        if (tier > 1)
            itemName = Text.join(Text.of("Tier" + RomanNumber.toRoman(tier)), itemName).color(itemName.style().color());
        return item.lore(itemLore).name(itemName.notItalic()).hide(TOOLTIP_PARTS);
    }

}
