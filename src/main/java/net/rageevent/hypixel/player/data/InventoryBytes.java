package net.rageevent.hypixel.player.data;

import dev.prism.api.inventory.Inventory;
import dev.prism.api.item.Item;
import dev.prism.api.nbt.Nbt;
import dev.prism.api.nbt.NbtCompound;
import dev.prism.api.nbt.NbtException;
import dev.prism.api.nbt.NbtList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Inventories as bytes, for a database column or a file. The bytes are gzipped NBT holding only the filled
 * slots, each item in the game's own shape ({id, count, components}) plus its Slot, so names, lore,
 * enchantments, damage and every other component come back exactly.
 */
public final class InventoryBytes {
    private InventoryBytes() {
    }

    /**
     * Bumped if the layout ever changes, so old bytes can still be read.
     */
    private static final int FORMAT = 1;
    /**
     * Bigger than any real inventory (a double chest is 54, a player 41): refuses corrupt sizes.
     */
    private static final int MAX_SLOTS = 1024;

    /**
     * Every slot of the inventory, as bytes.
     */
    public static byte[] serialize(final Inventory inventory) {
        final Item[] items = new Item[inventory.size()];
        for (int slot = 0; slot < items.length; slot++) {
            items[slot] = inventory.get(slot);
        }
        return serialize(items);
    }

    /**
     * Items by slot (null and empty slots are skipped), as bytes.
     */
    public static byte[] serialize(final Item[] items) {
        if (items.length > MAX_SLOTS) {
            throw new IllegalArgumentException("too many slots: " + items.length);
        }
        final List<Nbt> filled = new ArrayList<>();
        for (int slot = 0; slot < items.length; slot++) {
            final Item item = items[slot];
            if (item != null && !item.isEmpty()) {
                filled.add(item.toNbt().with("Slot", slot));
            }
        }
        final NbtCompound root = NbtCompound.builder()
                .put("format", FORMAT)
                .put("size", items.length)
                .put("items", NbtList.of(filled))
                .build();
        return gzip(Nbt.toBytes(root));
    }

    /**
     * The saved slots, as many as the saved inventory had; empty ones are Item.EMPTY. Safe on any thread.
     */
    public static Item[] deserialize(final byte[] data) {
        if (data == null || data.length == 0) {
            return new Item[0];
        }
        final NbtCompound root = Nbt.fromBytes(gunzip(data));
        final int format = root.getInt("format", 0);
        if (format != FORMAT) {
            throw new NbtException("unknown inventory format " + format);
        }
        final int size = root.getInt("size", 0);
        if (size < 0 || size > MAX_SLOTS) {
            throw new NbtException("bad inventory size " + size);
        }
        final Item[] items = new Item[size];
        Arrays.fill(items, Item.EMPTY);
        for (final Nbt entry : root.getList("items")) {
            if (!(entry instanceof NbtCompound item)) {
                throw new NbtException("expected an item, got " + entry);
            }
            final int slot = item.getInt("Slot", -1);
            if (slot < 0 || slot >= size) {
                throw new NbtException("slot " + slot + " is outside the inventory's " + size);
            }
            items[slot] = Item.fromNbt(item);
        }
        return items;
    }

    /**
     * Replaces everything in the inventory with the saved items. Returns any it had no slots for.
     */
    public static List<Item> load(final byte[] data, final Inventory into) {
        return load(deserialize(data), into);
    }

    /**
     * As load(byte[], Inventory), for items already deserialized (on another thread, say).
     */
    public static List<Item> load(final Item[] items, final Inventory into) {
        into.clear();
        final List<Item> didNotFit = new ArrayList<>();
        if (items == null) {
            return didNotFit;
        }
        for (int slot = 0; slot < items.length; slot++) {
            final Item item = items[slot];
            if (item == null || item.isEmpty()) {
                continue;
            }
            if (slot < into.size()) {
                into.set(slot, item);
            } else {
                didNotFit.add(item);
            }
        }
        return didNotFit;
    }

    private static byte[] gzip(final byte[] raw) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(raw.length / 2 + 32);
        try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
            gz.write(raw);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    private static byte[] gunzip(final byte[] data) {
        try (GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(data))) {
            return gz.readAllBytes();
        } catch (final IOException e) {
            throw new NbtException("these bytes are not a saved inventory", e);
        }
    }
}
