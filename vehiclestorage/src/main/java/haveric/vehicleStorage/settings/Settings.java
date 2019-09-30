package haveric.vehicleStorage.settings;

import haveric.vehicleStorage.VehicleStorage;
import haveric.vehicleStorage.messages.MessageSender;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Settings {
    private static FileConfiguration fileConfig;
    private static Settings instance;
    private static final String FILE_CONFIG = "config.yml";
    private static final String LASTCHANGED_CONFIG = "1.0.0";

    private static final boolean COLOR_CONSOLE_DEFAULT = true;
    private static final boolean DESTROY_PLAYER_ONLY_DEFAULT = true;

    protected Settings() {
        // Exists only to defeat instantiation.
    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();

            init();
        }

        return instance;
    }

    public static void clean() {
        instance = null;
    }

    private static void init() {
        // Load/reload/generate config.yml
        fileConfig = loadYML(FILE_CONFIG);
    }

    public void reload(CommandSender sender) {
        init();

        String lastChanged = fileConfig.getString("lastchanged");

        if (!LASTCHANGED_CONFIG.equals(lastChanged)) {
            MessageSender.getInstance().sendAndLog(sender, "<yellow>NOTE: <reset>'" + FILE_CONFIG + "' file is outdated, please delete it to allow it to be generated again.");
        }

        Storages.clean();
        ConfigurationSection storagesConfig = fileConfig.getConfigurationSection("storages");
        if (storagesConfig != null) {
            for (String storageString : storagesConfig.getKeys(false)) {
                ConfigurationSection storageConfig = fileConfig.getConfigurationSection("storages." + storageString);

                if (storageConfig != null) {
                    Storage storage = new Storage();
                    storage.setName(storageString);
                    for (String arg : storageConfig.getKeys(false)) {
                        String argLower = arg.toLowerCase();
                        String value = fileConfig.getString("storages." + storageString + "." + arg);

                        if (value != null) {
                            if (argLower.equals("entitytype")) {
                                storage.setEntityType(EntityType.valueOf(value.toUpperCase()));
                            } else if (argLower.equals("createitem")) {
                                storage.setCreateItem(new ItemStack(Material.valueOf(value.toUpperCase())));
                            } else if (argLower.equals("returnitems")) {
                                List<ItemStack> items = new ArrayList<>();
                                String[] split = value.split(",");
                                for (String s : split) {
                                    String mat = s.trim().toUpperCase();
                                    items.add(new ItemStack(Material.valueOf(mat)));
                                }
                                storage.setReturnItems(items);
                            } else if (argLower.equals("inventorysize")) {
                                storage.setInventorySize(Integer.parseInt(value));
                            } else if (argLower.equals("displayitem")) {
                                storage.setDisplayItem(new ItemStack(Material.valueOf(value.toUpperCase())));
                            } else if (argLower.equals("inventorytitle")) {
                                storage.setInventoryTitle(value);
                            }
                        }
                    }

                    Storages.add(storage);
                }
            }
        }
    }

    private static FileConfiguration loadYML(String fileName) {
        File file = new File(VehicleStorage.getPlugin().getDataFolder() + File.separator + fileName);

        if (!file.exists()) {
            VehicleStorage.getPlugin().saveResource(fileName, false);
            MessageSender.getInstance().log("Generated and loaded '" + fileName + "' file.");
        } else {
            MessageSender.getInstance().log("Loaded '" + fileName + "' file.");
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public boolean getColorConsole() {
        return fileConfig.getBoolean("color-console", COLOR_CONSOLE_DEFAULT);
    }

    public boolean getDestroyPlayerOnly() {
        return fileConfig.getBoolean("destroy-playeronly", DESTROY_PLAYER_ONLY_DEFAULT);
    }
}
