package me.w41k3r.shopkeepersAddon.gui.teleporter;

import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.registry;
import static me.w41k3r.shopkeepersAddon.gui.managers.PlayerShopsManager.fetchShopLocation;
import static me.w41k3r.shopkeepersAddon.gui.managers.PersistentGUIDataManager.getCurrentPage;

public class Teleporter {
    // This class is intended to handle teleportation-related functionalities.
    // Currently, it does not contain any methods or fields.
    // Future implementations may include:
    // - Teleporting players to specific locations
    // - Managing teleportation cooldowns
    // - Handling teleportation confirmations

    public static void teleportToShop(InventoryClickEvent event, UUID uuid) {
        Player player = (Player) event.getWhoClicked();
        int warmupTime;
        boolean allowMovement;
        String successMessage;
        String cancelMessage;
        String errorMessage = "Teleportation cancelled due to movement.";

        debugLog(getCurrentPage(event.getClickedInventory().getItem(49)));
        switch (getCurrentPage(event.getClickedInventory().getItem(49))) {
            case "ADMIN_SHOPS_LIST":
                Shopkeeper shopkeeper = registry.getShopkeeperByUniqueId(uuid);
                warmupTime = player.hasPermission("skp.adminshop.warmup.bypass")
                        ? 0
                        : config.getInt("adminShops.teleport.warmup");
                allowMovement = config.getBoolean("adminShops.teleport.allowMovement");
                successMessage = config.getString("messages.adminShops.teleport.success", "Teleporting to shop...");
                cancelMessage = config.getString("messages.adminShops.teleport.cancel", "Teleportation cancelled due to movement.");
                errorMessage = config.getString("messages.adminShops.teleport.noShop", "Teleportation cancelled due to movement.");
                Location teleportLocation = shopkeeper.getLocation();
                TeleportWarmup adminShopWarmup = new TeleportWarmup(
                        player,
                        player.getLocation(),
                        teleportLocation,
                        cancelMessage,
                        successMessage,
                        errorMessage,
                        allowMovement,
                        warmupTime,
                        plugin,
                        true,
                        shopkeeper.getUniqueId()
                );
                player.closeInventory();
                adminShopWarmup.startWarmup();

                break;
            case "PLAYER_SHOPS_LIST":
                Location shopLocation = fetchShopLocation(uuid);
                if (shopLocation == null) {
                    debugLog("No shop found for player: " + " (" + uuid + ")");
                    player.sendMessage(config.getString("messages.playerShops.teleport.noShop", "No Shop Found"));
                    return;
                }
                warmupTime = player.hasPermission("skp.playershop.warmup.bypass")
                        ? 0
                        : config.getInt("playerShops.teleport.warmup");
                allowMovement = config.getBoolean("playerShops.teleport.allowMovement");

                successMessage = config.getString("messages.playerShops.teleport.success", "Teleporting to shop...");
                cancelMessage = config.getString("messages.playerShops.teleport.cancel", "Teleportation cancelled due to movement.");
                errorMessage = config.getString("messages.playerShops.teleport.noShop", "Teleportation cancelled due to movement.");
                TeleportWarmup playerShopWarmup = new TeleportWarmup(
                        player,
                        player.getLocation(),
                        shopLocation,
                        cancelMessage,
                        successMessage,
                        errorMessage,
                        allowMovement,
                        warmupTime,
                        plugin,
                        false,
                        UUID.randomUUID()
                );
                player.closeInventory();
                playerShopWarmup.startWarmup();
                debugLog("Teleporting to player shop: " + uuid + " at location: " + shopLocation);
                break;
            default:
                debugLog("Unknown inventory type for teleportation: " + getCurrentPage(event.getClickedInventory().getItem(49)));
                player.sendMessage(config.getString("messages.teleport.error", "Unknown teleportation request."));
        }
    }


}
