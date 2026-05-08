package fr.traqueur.crates.views.buttons;

import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.engine.InventoryEngine;
import fr.traqueur.crates.Messages;
import fr.traqueur.crates.api.CratesPlugin;
import fr.traqueur.crates.api.managers.CratesManager;
import fr.traqueur.crates.api.models.crates.Crate;
import fr.traqueur.crates.api.models.crates.Reward;
import fr.traqueur.crates.api.models.crates.RewardsSorter;
import fr.traqueur.crates.api.providers.PlaceholderProvider;
import fr.traqueur.crates.api.services.ItemsService;
import fr.traqueur.crates.api.services.MessagesService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

public class PreviewButton extends PaginateButton {

    private static final DecimalFormat CHANCE_FORMAT = new DecimalFormat("#.##");

    private final CratesPlugin plugin;
    private final RewardsSorter sorter;

    public PreviewButton(Plugin plugin, RewardsSorter sorter) {
        this.plugin = (CratesPlugin) plugin;
        this.sorter = sorter;
    }

    @Override
    public void onRender(Player player, InventoryEngine inventoryEngine) {
        CratesManager cratesManager = this.plugin.getManager(CratesManager.class);

        Optional<Crate> crateOpt = cratesManager.getPreviewingCrate(player);
        if (crateOpt.isEmpty()) {
            return;
        }

        Crate crate = crateOpt.get();
        List<Reward> rewards = crate.rewards();
        if (rewards.isEmpty()) {
            return;
        }

        // Only eligible rewards (conditions met) contribute to the total weight
        List<Reward> eligibleRewards = rewards.stream()
                .filter(r -> r.conditions().isEmpty()
                        || r.conditions().stream().allMatch(c -> c.check(player, crate)))
                .toList();
        double totalWeight = eligibleRewards.stream().mapToDouble(Reward::weight).sum();

        // Sort all rewards for display (ineligible rewards still shown at 0%)
        List<Reward> sortedRewards = sorter.sort(rewards);

        this.paginate(sortedRewards, inventoryEngine, (slot, reward) -> {
            ItemStack item = reward.displayItem().build(player);
            boolean eligible = eligibleRewards.contains(reward);
            double chance = (eligible && totalWeight > 0) ? (reward.weight() / totalWeight) * 100 : 0.0;
            String papiString = PlaceholderProvider.parsePlaceholders(player, Messages.CHANCE_REWARD_LORE_LINE.get());
            Component chanceComponent = MessagesService.parseMessage(papiString, Placeholder.parsed("chance", CHANCE_FORMAT.format(chance)));
            ItemsService.addLoreLine(item, chanceComponent);
            inventoryEngine.addItem(slot, item);
        });
    }

    @Override
    public int getPaginationSize(Player player) {
        CratesManager cratesManager = this.plugin.getManager(CratesManager.class);

        Optional<Crate> crateOpt = cratesManager.getPreviewingCrate(player);
        return crateOpt.map(crate -> crate.rewards().size()).orElse(0);
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }
}
