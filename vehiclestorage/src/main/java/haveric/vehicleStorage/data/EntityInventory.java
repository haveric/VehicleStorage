package haveric.vehicleStorage.data;

import haveric.vehicleStorage.settings.Storage;
import haveric.vehicleStorage.settings.Storages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

@SerializableAs("EntityInventory")
public class EntityInventory implements ConfigurationSerializable {
    static {
        ConfigurationSerialization.registerClass(EntityInventory.class, "EntityInventory");
    }

    private Inventory inventory = null;
    private ArmorStand chestVisual = null;

    private UUID entityUUID = null;
    private List<ItemStack> items = null;
    private InventoryType inventoryType = null;
    private String storageName = null;

    private static final String ID_ENTITY_UUID = "entityUUID";
    private static final String ID_ITEM = "item";
    private static final String ID_INVENTORY_TYPE = "inventorytype";
    private static final String ID_INVENTORY_SIZE = "inventorysize";
    private static final String ID_STORAGE_NAME = "storagename";

    /**
     * dummy caller to initialize Serialization class
     */
    public static void init() { }

    public EntityInventory() { }

    @SuppressWarnings("unchecked")
    public EntityInventory(Map<String, Object> map) {
        try {
            Object obj;

            obj = map.get(ID_ENTITY_UUID);
            if (obj instanceof String) {
                entityUUID = UUID.fromString((String) obj);
            }

            int numItems = 0;
            obj = map.get(ID_INVENTORY_SIZE);
            if (obj instanceof Integer) {
                numItems = (Integer) obj;
            }

            items = new ArrayList<>();
            for (int i = 0; i < numItems; i++) {
                obj = map.get(ID_ITEM + i);
                if (obj instanceof Map) {
                    items.add(ItemStack.deserialize((Map<String, Object>) obj));
                } else {
                    items.add(null);
                }
            }

            obj = map.get(ID_INVENTORY_TYPE);
            if (obj instanceof String) {
                inventoryType = InventoryType.valueOf((String) obj);
            }

            obj = map.get(ID_STORAGE_NAME);
            if (obj instanceof String) {
                storageName = (String) obj;
            }
        } catch (Throwable e) {

        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        if (entityUUID != null) {
            map.put(ID_ENTITY_UUID, entityUUID.toString());
        }

        convertInventoryToItems();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                ItemStack item = items.get(i);
                if (item != null && item.getType() != Material.AIR) {
                    map.put(ID_ITEM + i, item.serialize());
                }
            }

            map.put(ID_INVENTORY_SIZE, items.size());
        }

        if (inventoryType != null) {
            map.put(ID_INVENTORY_TYPE, inventoryType.toString());
        }

        if (storageName != null) {
            map.put(ID_STORAGE_NAME, storageName);
        }

        return map;
    }

    private void convertInventoryToItems() {
        if (inventory != null) {
            items = Arrays.asList(inventory.getContents());
        }
    }

    public static EntityInventory deserialize(Map<String, Object> map) {
        return new EntityInventory(map);
    }

    public static EntityInventory valueOf(Map<String, Object> map) {
        return new EntityInventory(map);
    }

    public UUID getEntityUUID() {
        return entityUUID;
    }

    public void setEntityUUID(UUID entityUUID) {
        this.entityUUID = entityUUID;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(InventoryType inventoryType) {
        this.inventoryType = inventoryType;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public Inventory getInventory() {
        if (inventory == null) {
            String title;
            Storage storage = Storages.getStorage(storageName);
            if (storage == null) {
                title = "Vehicle Storage";
            } else {
                title = storage.getInventoryTitle();
            }

            if (inventoryType == InventoryType.CHEST) {
                inventory = Bukkit.createInventory(null, items.size(), title);
            } else {
                inventory = Bukkit.createInventory(null, inventoryType, title);
            }

            ItemStack[] itemArray = new ItemStack[items.size()];
            for (int i = 0; i < items.size(); i++) {
                itemArray[i] = items.get(i);
            }
            inventory.setContents(itemArray);
        }

        return inventory;
    }

    public void updateChestVisualLocation() {
        Entity entity = Bukkit.getEntity(entityUUID);
        if (entity != null) {
            Location entityLocation = entity.getLocation();
            Location newLocation;
            if (entity instanceof Boat) {
                Vector entityVector = entityLocation.getDirection().normalize().multiply(-.95);
                newLocation = entityLocation.clone().add(entityVector);
                newLocation.setY(entityLocation.getY() - 1.2);

            } else if (entity instanceof Minecart) {
                Location cloneLocation = entityLocation.clone();
                cloneLocation.setYaw(cloneLocation.getYaw() - 90);
                Vector entityVector = cloneLocation.getDirection().normalize().multiply(-.6);

                newLocation = entityLocation.clone().add(entityVector);
                newLocation.setY(entityLocation.getY() - 1.2);
                newLocation.setYaw(newLocation.getYaw() - 90);
            } else {
                newLocation = entityLocation.clone();
            }

            if (chestVisual == null) {
                chestVisual = (ArmorStand) entity.getWorld().spawnEntity(newLocation, EntityType.ARMOR_STAND);
                Storage storage = Storages.getStorage(storageName);

                if (storage == null) {
                    chestVisual.setHelmet(new ItemStack(Material.CHEST));
                } else {
                    chestVisual.setHelmet(storage.getDisplayItem());
                }
                chestVisual.setVisible(false);
                chestVisual.setGravity(false);
                chestVisual.setAI(false);
                chestVisual.setCollidable(false);
                chestVisual.setArms(false);
                chestVisual.setBasePlate(false);
            }

            chestVisual.teleport(newLocation);
        }
    }

    public void destroy(World world, Location location) {
        killChestVisual();

        convertInventoryToItems();
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                world.dropItem(location, item.clone());
            }
        }

        Storage storage = Storages.getStorage(storageName);
        if (storage == null) {
            world.dropItem(location, new ItemStack(Material.CHEST));
        } else {
            List<ItemStack> drops = storage.getReturnItems();
            for (ItemStack drop : drops) {
                world.dropItem(location, drop.clone());
            }
        }

        if (inventory != null) {
            inventory.clear();
            inventory = null;
        }
    }

    public void killChestVisual() {
        if (chestVisual != null) {
            chestVisual.remove();
        }
    }
}
