package fr.traqueur.crates.commands;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import fr.traqueur.crates.api.CratesPlugin;
import fr.traqueur.crates.api.managers.CratesManager;
import fr.traqueur.crates.api.models.crates.Crate;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BatchOpenCommand extends Command<@NotNull CratesPlugin> {

    public BatchOpenCommand(@NotNull CratesPlugin plugin) {
        super(plugin, "batchopen");

        this.setPermission("crates.command.batchopen");
        this.setDescription("Open a crate multiple times for a player without animation");

        this.addArg("player", Player.class);
        this.addArg("crate", Crate.class);
        this.addArg("amount", int.class);
    }

    @Override
    public void execute(CommandSender sender, Arguments arguments) {
        Player target = arguments.get("player");
        Crate crate = arguments.get("crate");
        int amount = arguments.get("amount");

        int opened = this.getPlugin().getManager(CratesManager.class)
                .batchOpenCrate(target, crate, amount);

        sender.sendMessage("Opened " + opened + "x crate '" + crate.id() + "' for " + target.getName());
    }
}
