package fr.traqueur.crates.api.models.crates;

import fr.traqueur.structura.annotations.Polymorphic;
import fr.traqueur.structura.api.Loadable;
import org.bukkit.entity.Player;

/**
 * Represents a condition that must be met before opening a crate or obtaining a reward.
 * Implementations can check permissions, cooldowns, or any other requirement.
 */
@Polymorphic
public interface Condition extends Loadable {

    /**
     * Checks if the player meets this condition.
     *
     * @param player the player to check
     * @param crate the crate being opened
     * @return true if the condition is met, false otherwise
     */
    boolean check(Player player, Crate crate);

    /**
     * Called when the player successfully opens the crate.
     * Use this for side effects like setting cooldowns.
     * Only relevant for crate-level conditions.
     *
     * @param player the player who opened the crate
     * @param crate the crate that was opened
     */
    default void onOpen(Player player, Crate crate) {
        // Default: no-op
    }

    /**
     * Gets the error message key to display when the condition is not met.
     *
     * @return the error message key for messages.yml
     */
    String errorMessageKey();
}
