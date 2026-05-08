package fr.traqueur.crates.commands;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import fr.traqueur.crates.Messages;
import fr.traqueur.crates.api.CratesPlugin;
import fr.traqueur.crates.api.models.crates.Crate;
import fr.traqueur.crates.api.models.crates.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GiveAllKeysCommand extends Command<@NotNull CratesPlugin> {

    public GiveAllKeysCommand(CratesPlugin plugin) {
        super(plugin, "giveallkeys");
        this.setPermission("crates.command.giveallkeys");
        this.setDescription("Give keys to all online players");

        this.addArg("crate", Crate.class);
        this.addArg("amount", Integer.class, (sender, args) -> List.of("1", "5", "10", "64"));
    }

    @Override
    public void execute(CommandSender sender, Arguments arguments) {
        Crate crate = arguments.get("crate");
        int amount = arguments.get("amount");

        if (amount <= 0) {
            Messages.INVALID_AMOUNT.send(sender);
            return;
        }

        Key key = crate.key();
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < amount; i++) {
                key.give(player);
            }
        }

        Messages.KEYS_GIVEN_ALL.send(sender,
                Placeholder.parsed("amount", String.valueOf(amount)),
                Placeholder.parsed("crate", crate.displayName()),
                Placeholder.parsed("count", String.valueOf(Bukkit.getOnlinePlayers().size()))
        );
    }
}