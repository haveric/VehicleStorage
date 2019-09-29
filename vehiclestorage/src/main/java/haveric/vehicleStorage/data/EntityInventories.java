package haveric.vehicleStorage.data;

import haveric.vehicleStorage.VehicleStorage;
import haveric.vehicleStorage.messages.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class EntityInventories {
    private static final String SAVE_EXTENSION = ".entityinventory";
    private static Map<UUID, EntityInventory> inventories = new LinkedHashMap<>();

    protected static void init() { }

    public static void clean() {
        inventories.clear();
    }

    public static EntityInventory get(UUID uuid) {
        return inventories.get(uuid);
    }

    public static void create(UUID uuid) {
        EntityInventory entityInventory = new EntityInventory();
        entityInventory.setEntityUUID(uuid);
        entityInventory.setInventoryType(InventoryType.CHEST);
        List<ItemStack> initialItems = new ArrayList<>();
        for (int i = 0; i < 27; i++) {
            initialItems.add(new ItemStack(Material.AIR));
        }
        entityInventory.setItems(initialItems);
        entityInventory.updateChestVisualLocation();

        inventories.put(uuid, entityInventory);
    }

    public static void remove(UUID uuid) {
        inventories.remove(uuid);
    }

    public static void load() {
        File dir = new File(VehicleStorage.getPlugin().getDataFolder() + File.separator + "save" + File.separator);
        if (!dir.exists()) {
            return;
        }

        FileConfiguration yml;

        File[] listOfFiles = dir.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (!file.isFile() || !file.getName().endsWith(SAVE_EXTENSION)) {
                    continue;
                }

                yml = YamlConfiguration.loadConfiguration(file);

                for (Map.Entry<String, Object> e : yml.getConfigurationSection("inventories").getValues(false).entrySet()) {
                    inventories.put(UUID.fromString(e.getKey()), (EntityInventory) e.getValue());
                }
            }
        }
    }

    public static void save() {
        for (Map.Entry<UUID, EntityInventory> entry : inventories.entrySet()) {
            entry.getValue().killChestVisual();
        }

        File dir = new File(VehicleStorage.getPlugin().getDataFolder() + File.separator + "save" + File.separator);
        if (!dir.exists() && !dir.mkdirs()) {
            MessageSender.getInstance().info("<red>Couldn't create directories: " + dir.getPath());
            return;
        }

        Map<UUID, Map<UUID, EntityInventory>> worldData = new HashMap<>();
        for (Map.Entry<UUID, EntityInventory> entry : inventories.entrySet()) {
            Entity entity = Bukkit.getEntity(entry.getKey());
            if (entity != null) {
                World world = entity.getWorld();
                Map<UUID, EntityInventory> entityInventory = worldData.computeIfAbsent(world.getUID(), k -> new HashMap<>());

                entityInventory.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<UUID, Map<UUID, EntityInventory>> worldEntry : worldData.entrySet()) {
            World world = Bukkit.getWorld(worldEntry.getKey());

            FileConfiguration yml = new YamlConfiguration();
            yml.set("id", worldEntry.getKey().toString());

            for (Map.Entry<UUID, EntityInventory> entry : worldEntry.getValue().entrySet()) {
                yml.set("inventories." + entry.getKey(), entry.getValue());
            }

            String worldString;
            if (world == null) {
                worldString = worldEntry.getKey().toString();
            } else {
                worldString = world.getName();
            }

            File file = new File(dir.getPath() + File.separator + worldString + SAVE_EXTENSION);

            try {
                yml.save(file);
            } catch (Throwable e) {
                MessageSender.getInstance().error(null, e, "Failed to create '" + file.getPath() + "' file!");
            }
        }
    }
}
