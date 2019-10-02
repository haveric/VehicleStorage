package haveric.vehicleStorage.listeners;

import haveric.vehicleStorage.VehicleStorage;
import haveric.vehicleStorage.data.EntityInventories;
import haveric.vehicleStorage.data.EntityInventory;
import haveric.vehicleStorage.settings.Settings;
import haveric.vehicleStorage.settings.Storage;
import haveric.vehicleStorage.settings.Storages;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EntityListener implements Listener {
    public EntityListener() { }

    public void clean() {
        HandlerList.unregisterAll(this);
    }

    public static void reload() {
        HandlerList.unregisterAll(VehicleStorage.getEntityListener());
        Bukkit.getPluginManager().registerEvents(VehicleStorage.getEntityListener(), VehicleStorage.getPlugin());
    }

    @EventHandler
    public void armorStandInteract(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (entity instanceof ArmorStand) {
            if (EntityInventories.isEntityInventoryArmorStand(entity.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void entityInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (Storages.checkEntityType(entity.getType())) {
            Player player = event.getPlayer();

            if (player.isSneaking() && event.getHand() == EquipmentSlot.HAND) {
                UUID entityUUID = entity.getUniqueId();
                EntityInventory inventory = EntityInventories.get(entityUUID);

                // No Inventory found
                if (inventory == null) {
                    ItemStack holding = player.getInventory().getItemInMainHand();
                    Storage storage = Storages.getStorage(holding, entity.getType());

                    if (storage != null) {
                        if (player.hasPermission("vehiclestorage.create." + storage.getName())) {
                            EntityInventories.create(entityUUID, storage);
                            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                                holding.setAmount(holding.getAmount() - 1);
                            }
                        }
                    }
                } else {
                    if (player.hasPermission("vehiclestorage.open." + inventory.getStorageName())) {
                        player.openInventory(inventory.getInventory());
                    }
                }
            }
        }
    }

    @EventHandler
    public void chunkLoad(ChunkLoadEvent event) {
        Entity[] entities = event.getChunk().getEntities();

        for (Entity entity : entities) {
            if (Storages.checkEntityType(entity.getType())) {
                EntityInventory inventory = EntityInventories.get(entity.getUniqueId());
                if (inventory != null) {
                    inventory.updateChestVisualLocation();
                }
            }
        }
    }

    @EventHandler
    public void chunkUnload(EntityTeleportEvent event) {
        Entity entity = event.getEntity();
        if (Storages.checkEntityType(entity.getType())) {
            EntityInventory inventory = EntityInventories.get(entity.getUniqueId());
            if (inventory != null) {
                inventory.killChestVisual();
            }
        }
    }

    @EventHandler
    public void vehicleMove(VehicleMoveEvent event) {
        Entity entity = event.getVehicle();
        if (Storages.checkEntityType(entity.getType())) {
            EntityInventory inventory = EntityInventories.get(entity.getUniqueId());
            if (inventory != null) {
                inventory.updateChestVisualLocation();
            }
        }
    }

    @EventHandler
    public void vehicleDestroy(VehicleDestroyEvent event) {
        Entity entity = event.getVehicle();
        if (Storages.checkEntityType(entity.getType())) {
            EntityInventory inventory = EntityInventories.get(entity.getUniqueId());
            if (inventory != null) {
                Entity attacker = event.getAttacker();
                if (attacker instanceof Player) {
                    Player player = (Player) attacker;
                    if (!player.hasPermission("vehiclestorage.destroy." + inventory.getStorageName())) {
                        event.setCancelled(true);
                        return;
                    }
                } else if (Settings.getInstance().getDestroyPlayerOnly()) {
                    event.setCancelled(true);
                    return;
                }

                inventory.destroy(entity.getWorld(), entity.getLocation());
                EntityInventories.remove(entity.getUniqueId());
            }
        }
    }
}
