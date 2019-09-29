package haveric.vehicleStorage;

import haveric.vehicleStorage.data.EntityInventories;
import haveric.vehicleStorage.data.EntityInventory;
import haveric.vehicleStorage.listeners.EntityListener;
import haveric.vehicleStorage.messages.MessageSender;
import org.bukkit.plugin.java.JavaPlugin;

public class VehicleStorage extends JavaPlugin {
    private static VehicleStorage plugin;
    private static EntityListener entityListener;

    @Override
    public void onEnable() {
        plugin = this;

        EntityInventory.init();
        EntityInventories.load();

        entityListener = new EntityListener();

        reload();
        MessageSender.getInstance().info("Enableddddd");
    }

    @Override
    public void onDisable() {
        EntityInventories.save();
        EntityInventories.clean();

        if (entityListener != null) {
            entityListener.clean();
        }
        entityListener = null;
    }

    private void reload() {
        MessageSender.init(true);
        EntityListener.reload();
    }

    public static VehicleStorage getPlugin() {
        return plugin;
    }

    public static EntityListener getEntityListener() {
        return entityListener;
    }
}
