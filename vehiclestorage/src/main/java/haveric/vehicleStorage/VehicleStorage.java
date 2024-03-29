package haveric.vehicleStorage;

import haveric.vehicleStorage.commands.DefaultCommand;
import haveric.vehicleStorage.commands.ReloadCommand;
import haveric.vehicleStorage.commands.UpdateCommand;
import haveric.vehicleStorage.data.EntityInventories;
import haveric.vehicleStorage.data.EntityInventory;
import haveric.vehicleStorage.listeners.EntityListener;
import haveric.vehicleStorage.messages.MessageSender;
import haveric.vehicleStorage.settings.Settings;
import haveric.vehicleStorage.settings.Storages;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class VehicleStorage extends JavaPlugin {
    private static VehicleStorage plugin;
    private static EntityListener entityListener;

    @Override
    public void onEnable() {
        plugin = this;

        EntityInventory.init();
        EntityInventories.load();
        Storages.init();
        Files.init();

        entityListener = new EntityListener();

        // Register commands
        getCommand("vehiclestorage").setExecutor(new DefaultCommand());
        getCommand("vehiclestoragereload").setExecutor(new ReloadCommand());
        getCommand("vehiclestorageupdate").setExecutor(new UpdateCommand());

        EntityListener.reload();

        reload(null);
    }

    @Override
    public void onDisable() {
        EntityInventories.save(true);
        EntityInventories.clean();

        if (entityListener != null) {
            entityListener.clean();
        }
        entityListener = null;

        Storages.clean();
        Settings.clean();
    }

    public void reload(CommandSender sender) {
        MessageSender.init(Settings.getInstance().getColorConsole());
        Settings.getInstance().reload(sender);
        Files.reload(sender); // (re)generate info files if they do not exist

        Updater.init(this, 345001, Settings.getInstance().getUpdateCheckApiToken());
    }

    public static VehicleStorage getPlugin() {
        return plugin;
    }

    public static EntityListener getEntityListener() {
        return entityListener;
    }
}
