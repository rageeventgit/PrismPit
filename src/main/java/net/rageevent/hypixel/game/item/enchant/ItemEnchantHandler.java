package net.rageevent.hypixel.game.item.enchant;

import dev.prism.api.inventory.PlayerInventory;
import dev.prism.api.item.Item;
import lombok.experimental.UtilityClass;
import net.rageevent.hypixel.game.item.ItemInfoKeys;
import net.rageevent.hypixel.game.item.StoredEnchant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class ItemEnchantHandler {


    public Item apply(Item item, ItemEnchant enchant, int level) {
        //For some reason on second enchants, the list becomes immutable, making it impossible to add enchantments.
        List<StoredEnchant> storedEnchants = new ArrayList<>(getAll(item));
        storedEnchants.add(new StoredEnchant(enchant.name(), level));
        return ItemInfoKeys.withInfo(item, ItemInfoKeys.ENCHANTMENTS, storedEnchants);
    }

    public Item remove(Item item, ItemEnchant enchant) {
        List<StoredEnchant> storedEnchants = getAll(item);
        storedEnchants.removeIf(storedEnchant -> storedEnchant.enchantId().equalsIgnoreCase(enchant.name()));
        return ItemInfoKeys.withInfo(item, ItemInfoKeys.ENCHANTMENTS, storedEnchants);
    }

    public List<StoredEnchant> getAll(Item item) {
        return ItemInfoKeys.info(item, ItemInfoKeys.ENCHANTMENTS, new ArrayList<>());
    }

    public List<StoredEnchant> fromInventory(PlayerInventory inventory) {
        List<StoredEnchant> storedEnchants = new ArrayList<>();
        Item mainHand = inventory.mainHand();
        Item leggings = inventory.leggings();

        if (!mainHand.isEmpty())
            storedEnchants.addAll(getAll(mainHand));
        if (!leggings.isEmpty())
            storedEnchants.addAll(getAll(leggings));
        return storedEnchants;
    }

    public Optional<StoredEnchant> get(Item item, ItemEnchant enchant) {
        return getAll(item).stream().filter(storedEnchant -> storedEnchant.enchantId().equalsIgnoreCase(enchant.name())).findFirst();
    }
}
