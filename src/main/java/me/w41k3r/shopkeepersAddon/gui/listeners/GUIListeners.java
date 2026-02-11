package me.w41k3r.shopkeepersAddon.gui.listeners;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import me.w41k3r.shopkeepersAddon.gui.objects.VirtualInventoryOwner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.debugLog;
import static me.w41k3r.shopkeepersAddon.gui.BaseGUIHandlers.handleGUIEvent; // Updated import
import static me.w41k3r.shopkeepersAddon.gui.teleporter.Teleporter.teleportToShop;
import static me.w41k3r.shopkeepersAddon.gui.managers.PersistentGUIDataManager.getTarget;

public class GUIListeners implements Listener {

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof VirtualInventoryOwner)
                || event.getCurrentItem() == null) {
            return;
        }
        event.setCancelled(true);
        String target = getTarget(event.getCurrentItem());
        UUID uuid = null;
        try {
            uuid = UUID.fromString(target);
        } catch (IllegalArgumentException | NullPointerException e) {
            debugLog("Invalid UUID target: " + target);
        }

        if (target != null) {
            if (target.equalsIgnoreCase("ignore")) {
                debugLog("Ignoring click on item: " + event.getCurrentItem());
                return;
            } else if (uuid != null) {
                teleportToShop(event, uuid);
                debugLog("Clicked item UUID: " + uuid);
                return;
            }
        }
        handleGUIEvent(event); // Updated method call
    }
}