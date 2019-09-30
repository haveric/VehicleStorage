package haveric.vehicleStorage.data;

import haveric.vehicleStorage.VehicleStorage;
import haveric.vehicleStorage.messages.MessageSender;
import haveric.vehicleStorage.settings.Storage;
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
    private static final int DATA_VERSION = 1;
    private static final String SAVE_EXTENSION = ".entityinventory";
    private static Map<UUID, EntityInventory> inventories = new LinkedHashMap<>();

    protected static void init() { }

    public static void clean() {
        inventories.clear();
    }

    public static EntityInventory get(UUID uuid) {
        return inventories.get(uuid);
    }

    public static void create(UUID uuid, Storage storage) {
        EntityInventory entityInventory = new EntityInventory();
        entityInventory.setEntityUUID(uuid);
        int size = storage.getInventorySize();

        if (size == 9 || size == 18 || size == 27 || size == 36 || size == 45 || size == 54) {
            entityInventory.setInventoryType(InventoryType.CHEST);
        } else if (size == 5) {
            entityInventory.setInventoryType(InventoryType.HOPPER);
        } else {
            entityInventory.setInventoryType(InventoryType.CHEST);
            size = 27;
        }

        List<ItemStack> initialItems = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            initialItems.add(new ItemStack(Material.AIR));
        }
        entityInventory.setItems(initialItems);
        entityInventory.setStorageName(storage.getName());
        entityInventory.updateChestVisualLocation();

        inventories.put(uuid, entityInventory);
    }

    public static void remove(UUID uuid) {
        inventories.remove(uuid);
    }

    public static void load() {
        long start = System.currentTimeMillis();

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

        MessageSender.getInstance().log("Loaded " + inventories.size() + " storages in " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
    }

    public static void save() {
        long start = System.currentTimeMillis();
        MessageSender.getInstance().log("Saving " + inventories.size() + " storages...");

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
            yml.set("version", DATA_VERSION);

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

        MessageSender.getInstance().log("Saved storages in " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds");
    }
}
