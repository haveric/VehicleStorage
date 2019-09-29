package haveric.vehicleStorage.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

    private static final String ID_ENTITY_UUID = "entityUUID";
    private static final String ID_ITEM = "item";
    private static final String ID_INVENTORY_TYPE = "inventorytype";
    private static final String ID_INVENTORY_SIZE = "inventorysize";

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
                if (item != null) {
                    map.put(ID_ITEM + i, item.serialize());
                }
            }

            map.put(ID_INVENTORY_SIZE, items.size());
        }

        if (inventoryType != null) {
            map.put(ID_INVENTORY_TYPE, inventoryType.toString());
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

    public Inventory getInventory() {
        if (inventory == null) {
            if (inventoryType == InventoryType.CHEST) {
                inventory = Bukkit.createInventory(null, items.size());
            } else {
                inventory = Bukkit.createInventory(null, inventoryType);
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
            if (chestVisual == null) {
                chestVisual = (ArmorStand) entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ARMOR_STAND);
                chestVisual.setHelmet(new ItemStack(Material.CHEST));
                chestVisual.setVisible(false);
                chestVisual.setGravity(false);
                chestVisual.setAI(false);
                chestVisual.setCollidable(false);
                chestVisual.setArms(false);
                chestVisual.setBasePlate(false);
            }

            Location entityLocation = entity.getLocation();
            if (entity instanceof Boat) {
                Vector entityVector = entityLocation.getDirection().normalize().multiply(-.95);
                Location newLocation = entityLocation.clone().add(entityVector);
                newLocation.setY(entityLocation.getY() - 1.2);
                chestVisual.teleport(newLocation);
            } else if (entity instanceof Minecart) {
                Location cloneLocation = entityLocation.clone();
                cloneLocation.setYaw(cloneLocation.getYaw() - 90);
                Vector entityVector = cloneLocation.getDirection().normalize().multiply(-.6);

                Location newLocation = entityLocation.clone().add(entityVector);
                newLocation.setY(entityLocation.getY() - 1.2);
                newLocation.setYaw(newLocation.getYaw() - 90);
                chestVisual.teleport(newLocation);
            }

        }
    }

    public void destroy(Location location) {
        killChestVisual();

        convertInventoryToItems();
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                location.getWorld().dropItem(location, item.clone());
            }
        }
        location.getWorld().dropItem(location, new ItemStack(Material.CHEST));

        inventory.clear();
        inventory = null;
    }

    public void killChestVisual() {
        if (chestVisual != null) {
            chestVisual.remove();
        }
    }
}
