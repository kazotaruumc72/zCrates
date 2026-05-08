package fr.traqueur.crates.api.managers;

import fr.traqueur.crates.api.models.crates.Crate;
import fr.traqueur.crates.api.models.crates.OpenResult;
import fr.traqueur.crates.api.models.animations.Animation;
import fr.traqueur.crates.api.models.crates.Reward;
import fr.traqueur.crates.api.models.placedcrates.DisplayType;
import fr.traqueur.crates.api.models.placedcrates.PlacedCrate;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Optional;

/**
 * Manager responsible for all crate-related operations including opening crates,
 * managing animations, handling rerolls, and managing placed crates in the world.
 *
 * <p>This is the main entry point for interacting with the crate system programmatically.
 * Obtain an instance via {@code plugin.getManager(CratesManager.class)}.</p>
 *
 * @see OpenResult
 * @see Crate
 * @see PlacedCrate
 */
public non-sealed interface CratesManager extends Manager {

    // ==================== Crate Opening ====================

    /**
     * Attempts to open a crate for a player with all validation checks.
     *
     * <p>This method performs the following checks in order:</p>
     * <ol>
     *     <li>Verifies the player has the required key</li>
     *     <li>Checks all configured conditions (permissions, cooldowns, etc.)</li>
     *     <li>Fires {@link fr.traqueur.crates.api.events.CratePreOpenEvent} (cancellable)</li>
     *     <li>Consumes the key</li>
     *     <li>Calls {@code onOpen()} on all conditions</li>
     *     <li>Opens the crate menu</li>
     * </ol>
     *
     * @param player the player attempting to open the crate
     * @param crate the crate to open
     * @return an {@link OpenResult} indicating success or the reason for failure
     */
    OpenResult tryOpenCrate(Player player, Crate crate);

    /**
     * Force opens a crate for a player, bypassing key checks and conditions.
     *
     * <p>Use this method for administrative purposes or when you've already
     * validated access. This will still fire the {@link fr.traqueur.crates.api.events.CrateOpenEvent}.</p>
     *
     * @param player the player to open the crate for
     * @param crate the crate to open
     * @param animation the animation to play
     */
    void openCrate(Player player, Crate crate, Animation animation);

    /**
     * Opens the preview menu for a crate, showing all possible rewards.
     *
     * <p>The preview menu allows players to see what rewards they can win
     * without consuming a key.</p>
     *
     * @param player the player to show the preview to
     * @param crate the crate to preview
     */
    void openPreview(Player player, Crate crate);

    /**
     * Gets the crate a player is currently previewing, if any.
     *
     * @param player the player to check
     * @return an Optional containing the crate being previewed, or empty if not previewing
     */
    Optional<Crate> getPreviewingCrate(Player player);

    /**
     * Closes the preview for a player and cleans up associated state.
     *
     * @param player the player whose preview to close
     */
    void closePreview(Player player);

    /**
     * Starts the animation for a player who has an open crate menu.
     *
     * <p>This method is typically called by the animation button in the crate menu.
     * It generates the reward and begins the animation phases.</p>
     *
     * @param player the player whose animation to start
     * @param inventory the inventory to animate in
     * @param slots the slots to use for the animation
     */
    void startAnimation(Player player, Inventory inventory, List<Integer> slots);

    // ==================== Reroll System ====================

    /**
     * Opens a crate multiple times for a player without triggering any animation.
     *
     * <p>Each opening validates key + conditions, fires {@link fr.traqueur.crates.api.events.CratePreOpenEvent},
     * consumes the key, generates a reward, logs it, gives it immediately, and fires
     * {@link fr.traqueur.crates.api.events.RewardGivenEvent}. Stops early if keys run out,
     * a condition fails, or the pre-open event is cancelled.
     * The amount is capped by {@link Crate#maxBatchSize()}.</p>
     *
     * @param player the player opening
     * @param crate  the crate to open
     * @param amount desired number of openings (capped by crate.maxBatchSize())
     * @return number of successful openings
     */
    int batchOpenCrate(Player player, Crate crate, int amount);

    /**
     * Opens a crate the maximum allowed number of times for a player without animation.
     * Uses the crate's own max-batch-size if set, otherwise falls back to the global config value.
     *
     * @param player the player opening
     * @param crate  the crate to open
     * @return number of successful openings
     */
    int batchOpenCrate(Player player, Crate crate);

    /**
     * Checks if a player can reroll their current reward.
     *
     * <p>A player can reroll if:</p>
     * <ul>
     *     <li>They have an active crate opening</li>
     *     <li>The animation has completed</li>
     *     <li>They have remaining rerolls</li>
     * </ul>
     *
     * @param player the player to check
     * @return true if the player can reroll, false otherwise
     */
    boolean canReroll(Player player);

    /**
     * Gets the number of rerolls remaining for a player's current crate opening.
     *
     * @param player the player to check
     * @return the number of remaining rerolls, or 0 if not opening a crate
     */
    int getRerollsRemaining(Player player);

    /**
     * Gets the current reward for a player's active crate opening.
     *
     * @param player the player to check
     * @return an Optional containing the current reward, or empty if not opening
     */
    Optional<Reward> getCurrentReward(Player player);

    /**
     * Performs a reroll for a player, generating a new reward and restarting the animation.
     *
     * <p>This method fires {@link fr.traqueur.crates.api.events.CrateRerollEvent} which can be cancelled.</p>
     *
     * @param player the player to reroll for
     * @return true if the reroll was successful, false if cancelled or not allowed
     */
    boolean reroll(Player player);

    /**
     * Checks if a player's animation has completed.
     *
     * @param player the player to check
     * @return true if animation is complete, false otherwise
     */
    boolean isAnimationCompleted(Player player);

    // ==================== Crate Lifecycle ====================

    /**
     * Stops all active crate openings, cancelling animations and closing inventories.
     *
     * <p>This is typically called during plugin shutdown.</p>
     */
    void stopAllOpening();

    /**
     * Closes a crate opening for a player and gives them their reward.
     *
     * <p>If the animation was completed, the current reward is given to the player
     * and {@link fr.traqueur.crates.api.events.RewardGivenEvent} is fired.</p>
     *
     * @param player the player whose crate to close
     */
    void closeCrate(Player player);

    /**
     * Ensures all inventory files exist for registered crates.
     *
     * <p>Creates default inventory files if they don't exist.</p>
     */
    void ensureInventoriesExist();

    // ==================== Placed Crates Management ====================

    /**
     * Places a crate at a location with the specified display.
     *
     * <p>The crate data is persisted in the chunk's PDC and the display
     * entity/block is spawned immediately.</p>
     *
     * @param crateId the ID of the crate to place
     * @param location the location to place the crate at
     * @param displayType the type of display (BLOCK, ENTITY, etc.)
     * @param displayValue the display value (material name, entity type, etc.)
     * @param yaw the rotation of the display
     * @return the created PlacedCrate instance
     * @throws IllegalArgumentException if no display factory exists for the type
     */
    PlacedCrate placeCrate(String crateId, Location location, DisplayType displayType, String displayValue, float yaw);

    /**
     * Removes a placed crate from the world.
     *
     * <p>This removes both the display and the persisted data.</p>
     *
     * @param placedCrate the placed crate to remove
     */
    void removePlacedCrate(PlacedCrate placedCrate);

    /**
     * Finds a placed crate by the block at a location.
     *
     * @param block the block to search for
     * @return an Optional containing the placed crate, or empty if not found
     */
    Optional<PlacedCrate> findPlacedCrateByBlock(Block block);

    /**
     * Finds a placed crate by its display entity.
     *
     * @param entity the entity to search for
     * @return an Optional containing the placed crate, or empty if not found
     */
    Optional<PlacedCrate> findPlacedCrateByEntity(Entity entity);

    /**
     * Loads all placed crates from a chunk's PDC and spawns their displays.
     *
     * <p>Called automatically on chunk load events.</p>
     *
     * @param chunk the chunk to load from
     */
    void loadPlacedCratesFromChunk(Chunk chunk);

    /**
     * Unloads placed crates from a chunk, removing their displays.
     *
     * <p>Called automatically on chunk unload events. Data is preserved in PDC.</p>
     *
     * @param chunk the chunk to unload
     */
    void unloadPlacedCratesFromChunk(Chunk chunk);

    /**
     * Loads all placed crates from all loaded chunks in all worlds.
     *
     * <p>Called during plugin initialization.</p>
     */
    void loadAllPlacedCrates();

    /**
     * Unloads all placed crates, removing all displays.
     *
     * <p>Called during plugin shutdown.</p>
     */
    void unloadAllPlacedCrates();

    /**
     * Gets all placed crates in a specific world.
     *
     * @param world the world to search in
     * @return a list of placed crates in the world
     */
    List<PlacedCrate> getPlacedCratesInWorld(World world);
}