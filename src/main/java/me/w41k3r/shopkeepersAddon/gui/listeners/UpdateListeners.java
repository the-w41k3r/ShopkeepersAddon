package me.w41k3r.shopkeepersAddon.gui.listeners;

import com.nisovin.shopkeepers.api.events.ShopkeeperEditedEvent;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.debugLog;
import static me.w41k3r.shopkeepersAddon.gui.init.InitAdminPages.createAdminItemsList;
import static me.w41k3r.shopkeepersAddon.gui.init.InitAdminPages.createAdminShopsList;
import static me.w41k3r.shopkeepersAddon.gui.init.InitPlayerPages.createPlayerItemsList;
import static me.w41k3r.shopkeepersAddon.gui.init.InitPlayerPages.createPlayerShopsList;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.lastUpdateTime;

public class UpdateListeners implements Listener {

    @EventHandler
    public void onShopEdit(ShopkeeperEditedEvent event) {
        updateShops(event.getShopkeeper());
    }

    public static void updateShops(@Nullable Shopkeeper shopkeeper) {

        debugLog(ChatColor.YELLOW + "Updating shops... " + lastUpdateTime);
        if (lastUpdateTime > System.currentTimeMillis() - 7000) {
            debugLog(ChatColor.RED + "Skipping Update " + lastUpdateTime);
            return; // Prevent too frequent updates
        }
        if (shopkeeper == null) {
            createPlayerShopsList();
            debugLog("Players Shops list refreshed!!");
            return;
        }
        if (shopkeeper instanceof PlayerShopkeeper) {
            createPlayerItemsList();
            debugLog("Players Items list refreshed!!");
            return;
        }
        createAdminShopsList();
        createAdminItemsList();
        debugLog("Admin Shops refreshed!!");
    }
}
