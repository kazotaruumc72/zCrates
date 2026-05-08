package fr.traqueur.crates.registries;

import fr.traqueur.crates.api.models.crates.Reward;
import fr.traqueur.crates.api.models.crates.RewardsSorter;
import fr.traqueur.crates.api.registries.RewardSortersRegistry;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZRewardSortersRegistry implements RewardSortersRegistry {

    private final Map<String, RewardsSorter> sorters;

    public ZRewardSortersRegistry() {
        this.sorters = new ConcurrentHashMap<>();

        //ascending
        this.register("ascending", (rewards) -> rewards.stream().sorted(Comparator.comparingDouble(Reward::weight)).toList());
        //descending
        this.register("descending", (rewards) -> rewards.stream().sorted((r1, r2) -> Double.compare(r2.weight(), r1.weight())).toList());

    }

    @Override
    public void register(String s, RewardsSorter item) {
        this.sorters.put(s, item);
    }

    @Override
    public RewardsSorter getById(String s) {
        return this.sorters.get(s);
    }

    @Override
    public List<RewardsSorter> getAll() {
        return this.sorters.values().stream().toList();
    }

    @Override
    public void clear() {
        this.sorters.clear();
    }
}
