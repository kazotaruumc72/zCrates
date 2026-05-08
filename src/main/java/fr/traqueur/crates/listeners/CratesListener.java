package fr.traqueur.crates.listeners;

import fr.traqueur.crates.api.managers.CratesManager;
import fr.traqueur.crates.api.models.crates.Crate;
import fr.traqueur.crates.api.models.crates.OpenResult;
import fr.traqueur.crates.api.models.placedcrates.PlacedCrate;
import fr.traqueur.crates.api.registries.CratesRegistry;
import fr.traqueur.crates.api.registries.Registry;
import fr.traqueur.crates.utils.OpenResultHandler;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public record CratesListener(CratesManager cratesManager) implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cratesManager.closeCrate(event.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory != null && inventory.getHolder() instanceof Crate) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (inventory.getHolder() instanceof Crate) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        cratesManager.loadPlacedCratesFromChunk(event.getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        cratesManager.unloadPlacedCratesFromChunk(event.getChunk());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockInteract(PlayerInteractEvent event) {
        // Only process main hand to avoid double execution
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Optional<PlacedCrate> placedCrateOpt = cratesManager.findPlacedCrateByBlock(block);
        if (placedCrateOpt.isEmpty()) {
            return;
        }

        PlacedCrate placedCrate = placedCrateOpt.get();
        Player player = event.getPlayer();

        // Left click = preview, Right click = open
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            previewPlacedCrate(player, placedCrate);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            if (player.isSneaking()) {
                batchOpenPlacedCrate(player, placedCrate);
            } else {
                openPlacedCrate(player, placedCrate);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        // Only process main hand to avoid double execution
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Entity entity = event.getRightClicked();

        Optional<PlacedCrate> placedCrateOpt = cratesManager.findPlacedCrateByEntity(entity);
        if (placedCrateOpt.isEmpty()) {
            return;
        }

        event.setCancelled(true);

        PlacedCrate placedCrate = placedCrateOpt.get();
        Player player = event.getPlayer();

        if (player.isSneaking()) {
            batchOpenPlacedCrate(player, placedCrate);
        } else {
            openPlacedCrate(player, placedCrate);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Optional<PlacedCrate> placedCrateOpt = cratesManager.findPlacedCrateByBlock(block);
        if (placedCrateOpt.isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        Optional<PlacedCrate> placedCrateOpt = cratesManager.findPlacedCrateByEntity(entity);
        if (placedCrateOpt.isPresent()) {
            event.setCancelled(true);
            if (!(event.getDamageSource().getCausingEntity() instanceof Player player)) {
                return;
            }
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                this.previewPlacedCrate(player, placedCrateOpt.get());
            }
        }
    }

    private void openPlacedCrate(Player player, PlacedCrate placedCrate) {
        CratesRegistry cratesRegistry = Registry.get(CratesRegistry.class);
        Crate crate = cratesRegistry.getById(placedCrate.crateId());
        if (crate == null) {
            return;
        }
        OpenResult result = cratesManager.tryOpenCrate(player, crate);
        if (result.isError()) {
            OpenResultHandler.getInstance().sendError(player, crate, result);
        }
    }

    private void batchOpenPlacedCrate(Player player, PlacedCrate placedCrate) {
        CratesRegistry cratesRegistry = Registry.get(CratesRegistry.class);
        Crate crate = cratesRegistry.getById(placedCrate.crateId());
        if (crate == null) return;
        cratesManager.batchOpenCrate(player, crate);
    }

    private void previewPlacedCrate(Player player, PlacedCrate placedCrate) {
        CratesRegistry cratesRegistry = Registry.get(CratesRegistry.class);
        Crate crate = cratesRegistry.getById(placedCrate.crateId());
        if (crate == null) {
            return;
        }
        cratesManager.openPreview(player, crate);
    }
}

