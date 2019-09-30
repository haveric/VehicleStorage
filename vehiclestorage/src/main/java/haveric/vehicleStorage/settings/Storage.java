package haveric.vehicleStorage.settings;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Storage {
    private String name;
    private EntityType entityType;
    private ItemStack createItem;
    private List<ItemStack> returnItems;
    private int inventorySize;
    private ItemStack displayItem;
    private String inventoryTitle;

    public Storage() {
        entityType = EntityType.BOAT;
        createItem = new ItemStack(Material.CHEST);
        returnItems = new ArrayList<>();
        returnItems.add(new ItemStack(Material.CHEST));

        inventorySize = 27;
        displayItem = new ItemStack(Material.CHEST);
        inventoryTitle = "Vehicle Storage";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public ItemStack getCreateItem() {
        return createItem;
    }

    public void setCreateItem(ItemStack createItem) {
        this.createItem = createItem;
    }

    public List<ItemStack> getReturnItems() {
        return returnItems;
    }

    public void setReturnItems(List<ItemStack> returnItems) {
        this.returnItems = returnItems;
    }

    public int getInventorySize() {
        return inventorySize;
    }

    public void setInventorySize(int inventorySize) {
        this.inventorySize = inventorySize;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public void setDisplayItem(ItemStack displayItem) {
        this.displayItem = displayItem;
    }

    public String getInventoryTitle() {
        return inventoryTitle;
    }

    public void setInventoryTitle(String inventoryTitle) {
        this.inventoryTitle = inventoryTitle;
    }
}
