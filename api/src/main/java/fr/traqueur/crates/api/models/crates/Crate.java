package fr.traqueur.crates.api.models.crates;

import fr.traqueur.crates.api.models.User;
import fr.traqueur.crates.api.models.algorithms.RandomAlgorithm;
import fr.traqueur.crates.api.models.animations.Animation;
import fr.traqueur.crates.api.settings.models.ItemStackWrapper;

import java.util.List;


/**
 * Represents a crate configuration that can be opened by players to receive rewards.
 *
 * <p>A crate defines all aspects of the loot box experience including:</p>
 * <ul>
 *     <li>The key required to open it</li>
 *     <li>The animation played during opening</li>
 *     <li>The algorithm used to select rewards</li>
 *     <li>The list of possible rewards with their weights</li>
 *     <li>Opening conditions (permissions, cooldowns, etc.)</li>
 * </ul>
 *
 * <p>Crates are loaded from YAML files in the {@code plugins/zCrates/crates/} directory.</p>
 *
 * <p><b>Example YAML configuration:</b></p>
 * <pre>{@code
 * id: legendary
 * animation: roulette
 * algorithm: weighted
 * display-name: "<gold>Legendary Crate"
 * max-rerolls: 3
 * key:
 *   type: VIRTUAL
 *   name: "legendary-key"
 * rewards:
 *   - type: ITEM
 *     id: diamond-reward
 *     weight: 10.0
 *     display-item:
 *       material: DIAMOND
 *     item:
 *       material: DIAMOND
 *       amount: 5
 * }</pre>
 *
 * @see Reward
 * @see Key
 * @see Animation
 * @see Condition
 */
public interface Crate {

    /**
     * Gets the unique identifier for this crate.
     *
     * <p>This ID is used in commands, configurations, and internal references.</p>
     *
     * @return the crate ID (e.g., "legendary", "common")
     */
    String id();

    /**
     * Gets the display name of this crate.
     *
     * <p>Supports MiniMessage formatting for colors and styles.</p>
     *
     * @return the formatted display name
     */
    String displayName();

    /**
     * Gets the key required to open this crate.
     *
     * @return the key configuration (virtual or physical)
     * @see Key
     */
    Key key();

    /**
     * Gets the animation to play when opening this crate.
     *
     * @return the animation configuration
     * @see Animation
     */
    Animation animation();

    /**
     * Gets the algorithm used to select rewards from this crate.
     *
     * @return the random selection algorithm
     * @see RandomAlgorithm
     */
    RandomAlgorithm algorithm();

    /**
     * Gets the zMenu inventory name associated with this crate.
     *
     * <p>This refers to an inventory file in {@code plugins/zCrates/inventories/}.</p>
     *
     * @return the menu identifier
     */
    String relatedMenu();

    /**
     * Gets all possible rewards from this crate.
     *
     * @return an unmodifiable list of rewards
     * @see Reward
     */
    List<Reward> rewards();

    /**
     * Gets the maximum number of rerolls allowed for this crate.
     *
     * <p>A value of 0 disables rerolling. When rerolling is enabled, players
     * can re-spin the animation to get a different reward.</p>
     *
     * @return the maximum reroll count
     */
    int maxRerolls();

    /**
     * Gets the maximum number of openings allowed in a single batch open.
     *
     * <p>A value of 0 disables batch opening for this crate.</p>
     *
     * @return the maximum batch size
     */
    int maxBatchSize();

    /**
     * Gets the conditions that must be met to open this crate.
     *
     * <p>All conditions must pass before the crate can be opened.
     * Common conditions include permissions and cooldowns.</p>
     *
     * @return the list of conditions, empty if no conditions
     * @see Condition
     */
    List<Condition> conditions();

    /**
     * Gets a random item to display in menus (preview filler).
     *
     * <p>This is typically used in animations to show random reward items
     * before the final reward is revealed.</p>
     *
     * @return a random display item from the rewards list
     */
    ItemStackWrapper randomDisplay();

    /**
     * Gets whether this crate instantly gives the reward without opening any menu or animation.
     *
     * @return true if the crate skips the animation and gives the reward immediately
     */
    boolean instantReward();

    /**
     * Generates a reward for a user using the configured algorithm.
     *
     * <p>This method uses the crate's {@link RandomAlgorithm} to select
     * a reward based on weights and the user's history.</p>
     *
     * @param user the user opening the crate (for history-based algorithms)
     * @return the selected reward
     * @see RandomAlgorithm
     */
    Reward generateReward(User user);
}
