package fr.traqueur.crates.models.rewards;

import fr.traqueur.crates.api.Logger;
import fr.traqueur.crates.api.models.crates.Condition;
import fr.traqueur.crates.api.models.crates.Reward;
import fr.traqueur.crates.api.settings.models.ItemStackWrapper;
import fr.traqueur.structura.annotations.Options;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ItemsListReward(String id, ItemStackWrapper displayItem, double weight, List<ItemStackWrapper> items,
                              @Options(optional = true) List<Condition> conditions) implements Reward {

    public ItemsListReward {
        if (conditions == null) conditions = new ArrayList<>();
    }

    @Override
    public void give(Player player) {
        Logger.debug("Giving item reward {} to player {}", id, player.getName());
        ItemStack[] itemStacks = items.stream().map(item -> item.build(player)).toArray(ItemStack[]::new);
        player.getInventory().addItem(itemStacks).forEach((__, itemStack) -> player.getWorld().dropItemNaturally(player.getLocation(), itemStack));
    }
}
