package fr.traqueur.crates.utils;

import fr.traqueur.crates.Messages;
import fr.traqueur.crates.api.models.crates.Condition;
import fr.traqueur.crates.api.models.crates.Crate;
import fr.traqueur.crates.api.models.crates.OpenResult;
import fr.traqueur.crates.models.conditions.CooldownCondition;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

/**
 * Handles sending error messages based on OpenResult status.
 */
public class OpenResultHandler {

    private static final OpenResultHandler INSTANCE = new OpenResultHandler();

    public static OpenResultHandler getInstance() {
        return INSTANCE;
    }

    private OpenResultHandler() {}

    /**
     * Sends the appropriate error message based on the OpenResult status.
     */
    public void sendError(Player player, Crate crate, OpenResult result) {
        switch (result.status()) {
            case NO_KEY -> Messages.NO_KEY.send(player);
            case CONDITION_FAILED -> {
                Condition condition = result.failedCondition();
                if (condition == null) {
                    return;
                }
                String errorKey = condition.errorMessageKey();
                switch (errorKey) {
                    case "no-permission" -> Messages.CONDITION_NO_PERMISSION.send(player);
                    case "cooldown" -> {
                        if (condition instanceof CooldownCondition cooldownCondition) {
                            long remaining = cooldownCondition.getRemainingCooldown(player, crate);
                            String formattedTime = formatDuration(remaining);
                            Messages.CONDITION_COOLDOWN.send(player, Placeholder.parsed("time", formattedTime));
                        } else {
                            Messages.CONDITION_COOLDOWN.send(player, Placeholder.parsed("time", "unknown"));
                        }
                    }
                }
            }
            case ALREADY_OPENING -> {
                Messages.ALREADY_OPENING.send(player);
            }
            case EVENT_CANCELLED -> {} // No message for cancelled events
            case SUCCESS -> {} // Should not happen
        }
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes < 60) {
            return minutes + "m " + seconds + "s";
        }
        long hours = minutes / 60;
        minutes = minutes % 60;
        return hours + "h " + minutes + "m";
    }
}
