package fr.traqueur.crates.hooks.placeholderapi;

import fr.traqueur.crates.api.CratesPlugin;
import fr.traqueur.crates.api.Logger;
import fr.traqueur.crates.api.annotations.AutoHook;
import fr.traqueur.crates.api.hooks.Hook;
import fr.traqueur.crates.api.models.crates.Condition;
import fr.traqueur.crates.api.providers.PlaceholderProvider;
import fr.traqueur.structura.registries.DefaultValueRegistry;
import fr.traqueur.structura.registries.PolymorphicRegistry;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

@AutoHook("PlaceholderAPI")
public class PAPIHook implements Hook, PlaceholderProvider {

    private final CratesPlugin plugin;

    public PAPIHook(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        PlaceholderProvider.Holder.setInstance(this);
        new CratesExpansion(plugin).register();
        DefaultValueRegistry.getInstance().register(ComparisonType.class, ComparisonType.Default.class, ComparisonType.Default::value);
        PolymorphicRegistry.get(Condition.class).register("PLACEHOLDER", PlaceholderCondition.class);
        Logger.info("PlaceholderAPI hook enabled. Registered PLACEHOLDER open condition, and PlaceholderProvider and placeholders expansion.");
    }

    @Override
    public String parse(Player player, String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        return PlaceholderAPI.setPlaceholders(player, string);
    }

}
