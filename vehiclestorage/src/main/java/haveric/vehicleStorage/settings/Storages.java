package haveric.vehicleStorage.settings;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

public class Storages {
    private static List<EntityType> entityTypes = new ArrayList<>();
    private static List<Storage> storages = new ArrayList<>();

    public static void init() { }

    public static void clean() {
        storages.clear();
    }

    public static void add(Storage storage) {
        storages.add(storage);

        EntityType entityType = storage.getEntityType();

        if (!entityTypes.contains(entityType)) {
            entityTypes.add(entityType);
        }

        String name = storage.getName();
        Permission perm = Bukkit.getPluginManager().getPermission("vehiclestorage.create." + name);
        if (perm == null) {
            perm = new Permission("vehiclestorage.create." + name, PermissionDefault.TRUE);
            perm.setDescription("Allows creation of " + name + " VehicleStorages");
            Bukkit.getPluginManager().addPermission(perm);
        }

        perm = Bukkit.getPluginManager().getPermission("vehiclestorage.open." + name);
        if (perm == null) {
            perm = new Permission("vehiclestorage.open." + name, PermissionDefault.TRUE);
            perm.setDescription("Allows opening of " + name + " VehicleStorage inventories");
            Bukkit.getPluginManager().addPermission(perm);
        }

        perm = Bukkit.getPluginManager().getPermission("vehiclestorage.destroy." + name);
        if (perm == null) {
            perm = new Permission("vehiclestorage.destroy." + name, PermissionDefault.TRUE);
            perm.setDescription("Allows player to destroy " + name + " VehicleStorages");
            Bukkit.getPluginManager().addPermission(perm);
        }
    }

    public static boolean checkEntityType(EntityType type) {
        return entityTypes.contains(type);
    }

    public static Storage getStorage(String name) {
        for (Storage storage : storages) {
            if (storage.getName().equals(name)) {
                return storage;
            }
        }

        return null;
    }

    public static Storage getStorage(ItemStack item, EntityType entityType) {
        ItemStack itemClone = item.clone();
        itemClone.setAmount(1);

        for (Storage storage : storages) {
            ItemStack createItemClone = storage.getCreateItem();
            createItemClone.setAmount(1);

            if (itemClone.hashCode() == createItemClone.hashCode() && storage.getEntityType() == entityType) {
                return storage;
            }
        }

        return null;
    }
}
