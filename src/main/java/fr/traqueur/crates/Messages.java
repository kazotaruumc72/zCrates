package fr.traqueur.crates;

import fr.traqueur.crates.api.services.MessagesService;
import fr.traqueur.structura.api.Loadable;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

public enum Messages implements Loadable {

    NO_PERMISSION("<red>You do not have permission to execute this command."),
    ONLY_IN_GAME("<red>This command can only be executed in-game."),
    ARG_NOT_RECOGNIZED("<red>Argument not recognized."),
    REQUIREMENT_NOT_MET("<red>You do not meet the requirements to perform this command."),
    COMMAND_DISABLED("<red>This command is currently disabled."),
    ARGUMENT_TOO_LONG("<red>Too many arguments provided."),
    INVALID_FORMAT("<red>Invalid command format."),

    INVALID_AMOUNT("<red>Amount must be greater than 0."),
    KEYS_GIVEN("<green>Gave x<amount> key(s) for the crate <crate> to <player>."),
    KEYS_GIVEN_ALL("<green>Gave x<amount> key(s) for the crate <crate> to <count> online player(s)."),

    // Placed crates messages
    CRATE_PLACED("<green>Successfully placed crate '<crate>' with display type <type>."),
    CRATE_REMOVED("<green>Successfully removed placed crate '<crate>'."),
    NO_CRATE_FOUND("<red>No placed crate found at your target location."),
    NO_KEY("<red>You don't have a key for this crate!"),
    DISPLAY_TYPE_NOT_AVAILABLE("<red>Display type <type> is not available. Is the required plugin installed?"),
    NO_CRATES_IN_CHUNK("<red>No placed crates found in this chunk."),
    CRATES_PURGED("<green>Successfully purged <count> placed crate(s) from this chunk."),

    CHANCE_REWARD_LORE_LINE("<gray>Chance: <chance>%"),

    // Reroll messages
    NO_REROLLS_LEFT("<red>You have no rerolls remaining!"),
    REROLL_SUCCESS("<green>Rerolled! <gray>(<remaining> rerolls left)"),

    // Condition messages
    CONDITION_NO_PERMISSION("<red>You don't have permission to open this crate!"),
    CONDITION_COOLDOWN("<red>You must wait <time> before opening this crate again!"),

    ALREADY_OPENING("<red>You are already opening a crate!")
    ;

    private final String rawMessage;

    Messages(String message) {
        this.rawMessage = message;
    }

    /**
     * Sends this message to a command sender with placeholders replaced.
     * <p>
     * Example usage:
     * <pre>{@code
     * Messages.EFFECT_APPLIED.send(
     *     player,
     *     Placeholder.parsed("effect", "super_hammer")
     * )
     * }</pre>
     *
     * @param sender       the command sender
     * @param placeholders the placeholders to replace
     */
    public void send(CommandSender sender, TagResolver... placeholders) {
        MessagesService.sendMessage(sender, this.rawMessage, placeholders);
    }

    public String get() {
        return this.rawMessage;
    }
}
