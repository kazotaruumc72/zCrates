package fr.traqueur.crates.api.registries;

import fr.traqueur.crates.api.models.crates.RewardsSorter;

import java.util.function.Function;

/**
 * Registry for reward sorters used in crate preview animations.
 * <p>
 * A rewards sorter is a function that takes a list of rewards and returns a new list sorted in a specific order.
 * This allows different sorting strategies to be applied to the rewards when they are displayed in the crate preview animation.
 */
public interface RewardSortersRegistry extends Registry<String, RewardsSorter> {
}
