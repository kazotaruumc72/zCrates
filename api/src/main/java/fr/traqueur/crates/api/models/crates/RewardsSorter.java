package fr.traqueur.crates.api.models.crates;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@FunctionalInterface
public interface RewardsSorter {

    @NotNull List<Reward> sort(@NotNull List<Reward> rewards);

}
