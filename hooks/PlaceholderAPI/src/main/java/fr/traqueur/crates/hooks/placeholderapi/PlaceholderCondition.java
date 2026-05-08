package fr.traqueur.crates.hooks.placeholderapi;

import fr.traqueur.crates.api.models.crates.Condition;
import fr.traqueur.crates.api.models.crates.Crate;
import fr.traqueur.crates.api.providers.PlaceholderProvider;
import org.bukkit.entity.Player;

import java.util.Objects;

public record PlaceholderCondition(String placeholder,
                                   @ComparisonType.Default ComparisonType comparison,
                                   String result) implements Condition {
    @Override
    public boolean check(Player player, Crate crate) {
        String placeholderValue = PlaceholderProvider.parsePlaceholders(player, placeholder);
        String expectedValue = PlaceholderProvider.parsePlaceholders(player, result);

        if (Objects.isNull(placeholderValue) || Objects.isNull(expectedValue)) {
            return false;
        }

        // Try to parse both values as numbers
        try {
            double actualNum = parseNumber(placeholderValue.trim());
            double expectedNum = parseNumber(expectedValue.trim());
            return comparison.apply(actualNum, expectedNum);
        } catch (NumberFormatException e) {
            // If parsing fails, fall back to string comparison for EQUALS and NOT_EQUALS
            if (comparison == ComparisonType.EQUALS) {
                return placeholderValue.equalsIgnoreCase(expectedValue);
            } else if (comparison == ComparisonType.NOT_EQUALS) {
                return !placeholderValue.equalsIgnoreCase(expectedValue);
            }
            return false;
        }
    }

    private double parseNumber(String value) throws NumberFormatException {
        // Remove common formatting characters (commas, spaces)
        String cleaned = value.replace(",", "").replace(" ", "");
        return Double.parseDouble(cleaned);
    }

    @Override
    public String errorMessageKey() {
        return "no-permission";
    }
}
