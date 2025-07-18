package me.w41k3r.shopkeepersAddon.gui.objects;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class VirtualInventoryOwner implements InventoryHolder {
    /*
    * Create a virtual inventory owner for the shopkeepers.
    *  */
    private final String name;
    private Inventory inventory;
    public VirtualInventoryOwner(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    // Implement the InventoryHolder method
    public Inventory getInventory() {
        return this.inventory;
    }
    // Set the inventory (or initialize it directly in the constructor)
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

}

