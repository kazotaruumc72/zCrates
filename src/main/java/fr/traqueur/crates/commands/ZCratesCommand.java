package fr.traqueur.crates.commands;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import fr.traqueur.crates.api.CratesPlugin;
import fr.traqueur.crates.commands.animations.AnimationsRootCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ZCratesCommand extends Command<@NotNull CratesPlugin> {

    /**
     * The constructor of the command.
     *
     * @param plugin The plugin that owns the command.
     */
    public ZCratesCommand(CratesPlugin plugin) {
        super(plugin, "zcrates");
        this.addAlias("zc", "crates", "crates");
        this.setPermission("zcrates.command.admin");
        this.setDescription("Main command for zCrates plugin.");

        this.addSubCommand(
                new ReloadCommand(plugin),
                new AnimationsRootCommand(plugin),
                new PlaceCrateCommand(plugin),
                new RemoveCrateCommand(plugin),
                new PurgeCratesCommand(plugin),
                new GiveKeysCommand(plugin),
                new GiveAllKeysCommand(plugin),
                new OpenCrateCommand(plugin),
                new BatchOpenCommand(plugin)
        );
    }

    @Override
    public void execute(CommandSender sender, Arguments arguments) {
        sender.sendMessage("zCrates Plugin - Version " + this.getPlugin().getPluginMeta().getVersion());
    }
}
