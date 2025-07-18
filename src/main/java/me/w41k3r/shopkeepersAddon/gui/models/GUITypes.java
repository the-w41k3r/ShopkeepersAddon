package me.w41k3r.shopkeepersAddon.gui.models;

import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

import static me.w41k3r.shopkeepersAddon.gui.models.Variables.*;

public enum GUITypes {
    /*
    Consider these variables have already been defined in Variables.java

    public static Inventory adminShopsPage;
    public static Inventory playerShopsPage;
    public static Inventory homePage;

    public static ArrayList<Inventory> adminShopsList = new ArrayList<>();
    public static ArrayList<Inventory> adminItemsList = new ArrayList<>();
    public static ArrayList<Inventory> playerShopsList = new ArrayList<>();
    public static ArrayList<Inventory> playerItemsList = new ArrayList<>();
    * */
    ADMIN_SHOPS_PAGE(adminShopsPage),
    PLAYER_SHOPS_PAGE(playerShopsPage),
    HOME_PAGE(homePage),

    ADMIN_SHOPS_LIST(adminShopsList),
    ADMIN_ITEMS_LIST(adminItemsList),
    PLAYER_SHOPS_LIST(playerShopsList),
    PLAYER_ITEMS_LIST(playerItemsList);

    private final Inventory inventory;
    private final ArrayList<Inventory> inventoryList;

    GUITypes(Inventory inventory) {
        this.inventory = inventory;
        this.inventoryList = null; // No list for this constructor
    }

    GUITypes(ArrayList<Inventory> inventoryList) {
        this.inventoryList = inventoryList;
        this.inventory = null; // No single inventory for this constructor
    }

    public Inventory getInventory() {
        return inventory;
    }

    public ArrayList<Inventory> getInventoryList() {
        return inventoryList;
    }
}
