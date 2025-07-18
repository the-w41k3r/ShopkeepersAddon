package me.w41k3r.shopkeepersAddon.gui.listeners;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import me.w41k3r.shopkeepersAddon.gui.objects.VirtualInventoryOwner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.debugLog;
import static me.w41k3r.shopkeepersAddon.gui.BaseGUIHandlers.OpenGUI;
import static me.w41k3r.shopkeepersAddon.gui.teleporter.Teleporter.teleportToShop;
import static me.w41k3r.shopkeepersAddon.gui.managers.PersistentGUIDataManager.getTarget;

public class GUIListeners implements Listener {

    /* * This class is intended to handle GUI-related events.
     * Currently, it does not contain any methods or fields.
     *
     * Future implementations may include:
     * - Handling clicks on the home page buttons
     * - Managing shopkeeper interactions
     * - Updating inventories based on player actions
     *
    * */

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof VirtualInventoryOwner)
                || event.getCurrentItem() == null
        ) {
            return;
        }
        event.setCancelled(true);
        String target = getTarget(event.getCurrentItem());
        UUID uuid = null;
        try {
            uuid = UUID.fromString(target);
        }
        catch (IllegalArgumentException e) {}
        catch (NullPointerException e) {}

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
        OpenGUI(event);
    }

}
