package me.w41k3r.shopkeepersaddon.General;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class VirtualOwner implements InventoryHolder {
    private final String name;
    private Inventory inventory;

    public VirtualOwner(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Implement the InventoryHolder method
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    // Set the inventory (or initialize it directly in the constructor)
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
