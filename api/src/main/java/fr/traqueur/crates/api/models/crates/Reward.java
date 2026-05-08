package fr.traqueur.crates.api.models.crates;

import fr.traqueur.crates.api.settings.models.ItemStackWrapper;
import fr.traqueur.structura.annotations.Polymorphic;
import fr.traqueur.structura.api.Loadable;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Represents a reward that can be won from a crate.
 *
 * <p>Rewards use polymorphic deserialization based on the "type" field in YAML.
 * The following reward types are available:</p>
 * <ul>
 *     <li>{@code ITEM} - A single item</li>
 *     <li>{@code ITEMS} - Multiple items</li>
 *     <li>{@code COMMAND} - A single console command</li>
 *     <li>{@code COMMANDS} - Multiple console commands</li>
 * </ul>
 *
 * <p><b>Example YAML configurations:</b></p>
 * <pre>{@code
 * # Single item reward
 * - type: ITEM
 *   id: diamond-sword
 *   weight: 5.0
 *   display-item:
 *     material: DIAMOND_SWORD
 *     name: "<red>Legendary Sword"
 *   item:
 *     material: DIAMOND_SWORD
 *     enchantments:
 *       SHARPNESS: 5
 *
 * # Command reward
 * - type: COMMAND
 *   id: money-reward
 *   weight: 20.0
 *   display-item:
 *     material: GOLD_INGOT
 *     name: "<yellow>$1000"
 *   command: "eco give %player% 1000"
 * }</pre>
 *
 * @see Crate
 */
@Polymorphic()
public interface Reward extends Loadable {

    /**
     * Gets the unique identifier for this reward within the crate.
     *
     * <p>Used for logging, history tracking, and algorithm references.</p>
     *
     * @return the reward ID
     */
    String id();

    /**
     * Gets the weight of this reward for random selection.
     *
     * <p>Higher weight means higher probability of being selected.
     * The probability is calculated as: {@code weight / totalWeights}.</p>
     *
     * <p>Example: With rewards weighted 10, 20, 70:</p>
     * <ul>
     *     <li>10 weight = 10% chance</li>
     *     <li>20 weight = 20% chance</li>
     *     <li>70 weight = 70% chance</li>
     * </ul>
     *
     * @return the weight value (higher = more common)
     */
    double weight();

    /**
     * Gets the item to display in preview menus and animations.
     *
     * <p>This is what players see before receiving the reward.
     * It may differ from the actual reward item.</p>
     *
     * @return the display item configuration
     */
    ItemStackWrapper displayItem();

    /**
     * Gets the conditions that must be met for this reward to be eligible.
     *
     * <p>If any condition is not met for the player, the reward's effective weight
     * is treated as 0 (never selected, shown as 0% in preview).</p>
     *
     * @return the list of conditions, empty if no conditions
     * @see Condition
     */
    default List<Condition> conditions() {
        return List.of();
    }

    /**
     * Gives this reward to a player.
     *
     * <p>The implementation depends on the reward type:</p>
     * <ul>
     *     <li>ITEM/ITEMS: Adds items to inventory (drops if full)</li>
     *     <li>COMMAND/COMMANDS: Executes console commands with %player% placeholder</li>
     * </ul>
     *
     * @param player the player to give the reward to
     */
    void give(Player player);

}
