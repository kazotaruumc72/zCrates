package fr.traqueur.crates.views.loaders;

import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import fr.maxlego08.menu.api.loader.ButtonLoader;
import fr.traqueur.crates.api.models.crates.RewardsSorter;
import fr.traqueur.crates.api.registries.Registry;
import fr.traqueur.crates.api.registries.RewardSortersRegistry;
import fr.traqueur.crates.views.buttons.PreviewButton;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class PreviewLoader extends ButtonLoader {

    private final RewardSortersRegistry registry;

    public PreviewLoader(Plugin plugin, String name) {
        super(plugin, name);
        this.registry = Registry.get(RewardSortersRegistry.class);
    }

    @Override
    public Button load(YamlConfiguration yamlConfiguration, String s, DefaultButtonValue defaultButtonValue) {
        String sortType = yamlConfiguration.getString(s + ".sort", "ascending");
        RewardsSorter sorter = registry.getById(sortType);
        if (sorter == null) {
            throw new IllegalArgumentException("Invalid type for preview button: " + sortType);
        }
        return new PreviewButton(this.plugin, sorter);
    }
}
