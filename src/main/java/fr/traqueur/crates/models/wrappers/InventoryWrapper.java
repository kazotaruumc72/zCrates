package fr.traqueur.crates.models.wrappers;

import fr.traqueur.crates.api.CratesPlugin;
import fr.traqueur.crates.api.Logger;
import fr.traqueur.crates.api.models.crates.Crate;
import fr.traqueur.crates.api.models.Wrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Wrapper for Inventory that exposes only safe methods for animations.
 * This controls what animations can do with the crate inventory.
 */
public class InventoryWrapper extends Wrapper<Inventory> {

    private final CratesPlugin plugin;
    private final Player player;
    private final Crate crate;
    private final List<Integer> authorizedSlots;

    public InventoryWrapper(CratesPlugin plugin, Player player, Crate crate, Inventory delegate, List<Integer> authorizedSlots) {
        super(delegate);
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;
        this.authorizedSlots = authorizedSlots;
    }

    public void close(int delayTicks) {
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> player.closeInventory(), delayTicks);
    }

    public void closeImmediately() {
        player.closeInventory();
    }

    /**
     * Checks if a slot is authorized for modification.
     *
     * @param slot the slot index
     * @return true if the slot is authorized
     */
    private boolean isAuthorized(int slot) {
        return authorizedSlots.contains(slot);
    }

    /**
     * Clears all authorized slots in the inventory.
     */
    public void clear() {
        for (int slot : authorizedSlots) {
            if (slot >= 0 && slot < delegate.getSize()) {
                delegate.setItem(slot, null);
            }
        }
    }

    /**
     * Clears a specific slot if authorized.
     *
     * @param slot the slot index
     */
    public void clear(int slot) {
        if (isAuthorized(slot) && slot >= 0 && slot < delegate.getSize()) {
            delegate.setItem(slot, null);
        }
    }

    /**
     * Sets an item at a specific slot if authorized.
     *
     * @param slot the slot index
     * @param item the item to set
     */
    public void setItem(int slot, ItemStack item) {
        if (isAuthorized(slot) && slot >= 0 && slot < delegate.getSize()) {
            delegate.setItem(slot, item);
        }
    }

    /**
     * Sets a random item at the specified slot if authorized.
     * Implementation should be provided by the plugin.
     *
     * @param slot the slot index
     */
    public void setRandomItem(int slot) {
        if (isAuthorized(slot) && slot >= 0 && slot < delegate.getSize()) {
            ItemStack randomItem = crate.randomDisplay().build(player).clone();
            delegate.setItem(slot, randomItem);
        }
    }

    /**
     * Rotates items through the specified slots (shifts them to the right).
     * Only rotates slots that are authorized.
     * <p>
     * Accepts both Java int[] arrays and JavaScript arrays (NativeArray).
     * JavaScript arrays will be automatically converted to int[].
     *
     * @param slots the slot indices to rotate (int[] or JavaScript array)
     */
    public void rotateItems(Object slots) {
        // Convert to int[] array (handles both Java and JavaScript arrays)
        int[] slotArray = ArrayHelper.toIntArray(slots);
        if (slotArray == null || slotArray.length < 2) {
            return;
        }

        // Check all slots are authorized
        for (int slot : slotArray) {
            if (!isAuthorized(slot)) {
                Logger.warning("Slot " + slot + " is not authorized for rotation");
                return;
            }
        }

        // Save the last item
        ItemStack lastItem = delegate.getItem(slotArray[slotArray.length - 1]);

        // Shift all items to the right
        for (int i = slotArray.length - 1; i > 0; i--) {
            ItemStack item = delegate.getItem(slotArray[i - 1]);
            delegate.setItem(slotArray[i], item);
        }

        // Put the last item at the first position
        delegate.setItem(slotArray[0], lastItem);
    }

    /**
     * Rotates items through the specified slots (shifts them to the right),
     * and injects a new random display item at the first slot.
     * Produces an "infinite scroll" effect — the entering item is always fresh.
     *
     * @param slots the slot indices to rotate (int[] or JavaScript array)
     */
    public void rotateRandom(Object slots) {
        int[] slotArray = ArrayHelper.toIntArray(slots);
        if (slotArray == null || slotArray.length < 2) {
            return;
        }

        for (int slot : slotArray) {
            if (!isAuthorized(slot)) {
                Logger.warning("Slot " + slot + " is not authorized for rotation");
                return;
            }
        }

        // Shift all items to the right (last item is discarded)
        for (int i = slotArray.length - 1; i > 0; i--) {
            ItemStack item = delegate.getItem(slotArray[i - 1]);
            delegate.setItem(slotArray[i], item);
        }

        // Inject a fresh random item at the entry slot
        ItemStack newItem = crate.randomDisplay().build(player).clone();
        delegate.setItem(slotArray[0], newItem);
    }

    /**
     * Sets the winning item at the specified slot if authorized.
     *
     * @param slot   the slot index
     * @param reward the reward object
     */
    public void setWinningItem(int slot, ItemStack reward) {
        if (isAuthorized(slot) && slot >= 0 && slot < delegate.getSize()) {
            this.delegate.setItem(slot, reward);
        }
    }

    public int size() {
        return delegate.getSize();
    }

    /**
     * Highlights a slot with a specific material if authorized (typically glass panes).
     *
     * @param slot     the slot index
     * @param material the material name (e.g., "YELLOW_STAINED_GLASS_PANE")
     */
    public void highlightSlot(int slot, String material) {
        if (isAuthorized(slot) && slot >= 0 && slot < delegate.getSize()) {
            try {
                Material mat = Material.valueOf(material);
                ItemStack highlightItem = new ItemStack(mat);
                delegate.setItem(slot, highlightItem);
            } catch (IllegalArgumentException e) {
                Logger.debug("Invalid material for highlight: " + material);
            }
        }
    }

    /**
     * Gets the inventory size.
     *
     * @return the number of slots
     */
    public int getSize() {
        return delegate.getSize();
    }

    /**
     * Gets the list of authorized slots.
     *
     * @return the authorized slots
     */
    public List<Integer> getAuthorizedSlots() {
        return authorizedSlots;
    }

    /**
     * Gets an item at a specific slot.
     *
     * @param slot the slot index
     * @return the item, or null if empty
     */
    public ItemStack getItem(int slot) {
        if (slot >= 0 && slot < delegate.getSize()) {
            return delegate.getItem(slot);
        }
        return null;
    }
}