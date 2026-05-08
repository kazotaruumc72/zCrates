package fr.traqueur.crates.models.conditions;

import fr.traqueur.crates.api.models.crates.Condition;
import fr.traqueur.crates.api.models.crates.Crate;
import org.bukkit.entity.Player;

public record PermissionCondition(String permission) implements Condition {

    @Override
    public boolean check(Player player, Crate crate) {
        return player.hasPermission(permission);
    }

    @Override
    public String errorMessageKey() {
        return "no-permission";
    }
}
