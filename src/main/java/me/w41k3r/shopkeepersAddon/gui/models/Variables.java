package me.w41k3r.shopkeepersAddon.gui.models;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.ShopkeeperRegistry;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.plugin;

public class Variables {
    public static ShopkeeperRegistry registry = ShopkeepersAPI.getShopkeeperRegistry();
    public static Inventory adminShopsPage;
    public static Inventory playerShopsPage;
    public static Inventory homePage;

    public static ArrayList<Inventory> adminShopsList = new ArrayList<>();
    public static ArrayList<Inventory> adminItemsList = new ArrayList<>();
    public static ArrayList<Inventory> playerShopsList = new ArrayList<>();
    public static ArrayList<Inventory> playerItemsList = new ArrayList<>();

    public static HashMap<ItemStack, ArrayList<UUID>> playerItemOwners = new HashMap<>();
    public static HashMap<ItemStack, ArrayList<UUID>> adminItemOwners = new HashMap<>();

    public static final int BACK_BUTTON_SLOT = 49;
    public static final int NEXT_BUTTON_SLOT = 53;
    public static final int PREV_BUTTON_SLOT = 45;
    public static final int CURR_ITEM_SLOT = 47;


    public static String SHOPS_SAVEPATH = plugin.getDataFolder().toPath().resolve("shops").toString();

    public static String SKIN_CACHE_DIR = plugin.getDataFolder().toPath().resolve("skins").toString();

    public static long lastUpdateTime = 0;

    public static List<String> blacklistedItems = new ArrayList<>();
}
