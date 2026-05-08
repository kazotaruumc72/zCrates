package fr.traqueur.crates.models.conditions;

import fr.traqueur.crates.api.CratesPlugin;
import fr.traqueur.crates.api.models.crates.Condition;
import fr.traqueur.crates.api.models.crates.Crate;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public record CooldownCondition(long cooldown) implements Condition {

    private static final CratesPlugin PLUGIN = CratesPlugin.getPlugin(CratesPlugin.class);

    @Override
    public boolean check(Player player, Crate crate) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = getCooldownKey(crate.id());

        Long lastOpened = pdc.get(key, PersistentDataType.LONG);
        if (lastOpened == null) {
            return true;
        }

        long elapsed = System.currentTimeMillis() - lastOpened;
        return elapsed >= cooldown;
    }

    @Override
    public void onOpen(Player player, Crate crate) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = getCooldownKey(crate.id());
        pdc.set(key, PersistentDataType.LONG, System.currentTimeMillis());
    }

    @Override
    public String errorMessageKey() {
        return "cooldown";
    }

    private NamespacedKey getCooldownKey(String crateId) {
        return new NamespacedKey(PLUGIN, "cooldown_" + crateId);
    }

    /**
     * Gets the remaining cooldown time in milliseconds.
     *
     * @param player the player to check
     * @param crate the crate
     * @return remaining time in ms, or 0 if no cooldown
     */
    public long getRemainingCooldown(Player player, Crate crate) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = getCooldownKey(crate.id());

        Long lastOpened = pdc.get(key, PersistentDataType.LONG);
        if (lastOpened == null) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - lastOpened;
        return Math.max(0, cooldown - elapsed);
    }
}
