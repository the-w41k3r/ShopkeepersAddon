package me.w41k3r.shopkeepersaddon.General;

import com.nisovin.shopkeepers.api.events.ShopkeeperEditedEvent;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static me.w41k3r.shopkeepersaddon.General.UIHandler.*;
import static me.w41k3r.shopkeepersaddon.General.Utils.*;
import static me.w41k3r.shopkeepersaddon.Main.ShopkeepersInstance;
import static me.w41k3r.shopkeepersaddon.Main.plugin;

public class UpdateListeners implements Listener {


    @EventHandler
    public void ShopkeeperEdited(ShopkeeperEditedEvent event) {
        refreshShops();
    }


    public static void refreshShops(){
        new BukkitRunnable() {
            @Override
            public void run() {
                File dataFolder = ShopkeepersInstance.getDataFolder();
                File saveFolder = new File(dataFolder, "data/save.yml");
                debugLog("Loading shops from " + saveFolder);

                if (!saveFolder.exists()) {
                    errorLog("Save file does not exist: " + saveFolder);
                    return;
                }

                Map<String, Object> obj = null;
                try (FileInputStream fis = new FileInputStream(saveFolder)) {
                    Yaml yaml = new Yaml();
                    obj = yaml.load(fis);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                initializeUIs();
                for (String key : obj.keySet()) {
                    if (key.equalsIgnoreCase("data-version")) {
                        continue;
                    }
                    Map<String, Object> shopkeeper = (Map<String, Object>) obj.get(key);
                    addShopstoUI(shopkeeper);  // This interacts with the Bukkit API
                    addItemsToUI(shopkeeper);  // This interacts with the Bukkit API
                }

                debugLog("Shops loaded successfully!");

            }
        }.runTaskAsynchronously(plugin);  // Run the file loading asynchronously
    }


}
