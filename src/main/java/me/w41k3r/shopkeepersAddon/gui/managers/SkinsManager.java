package me.w41k3r.shopkeepersAddon.gui.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.SKIN_CACHE_DIR;
import static me.w41k3r.shopkeepersAddon.gui.managers.PersistentGUIDataManager.setPageNumber;
import static me.w41k3r.shopkeepersAddon.gui.managers.PersistentGUIDataManager.setTarget;
import static org.bukkit.Bukkit.createPlayerProfile;

public class SkinsManager {

    /* --------------------------------
     * Fetch the skin of the target player.
     * This will return a URL link such as https://textures.minecraft.net/texture/d5c4ee5ce20aed9e33e866c66caa37178606234b3721084bf01d13320fb2eb3f
     * -------------------------------- */
    public static byte[] fetchSkinByte(Player player) {
        try {
            PlayerTextures texture = player.getPlayerProfile().getTextures();
            debugLog("Skin: " + texture.getSkin());
            return texture.getSkin().toString().getBytes();
        } catch (Exception e) {
            String defaultSkin = "https://textures.minecraft.net/texture/d5c4ee5ce20aed9e33e866c66caa37178606234b3721084bf01d13320fb2eb3f";
            return defaultSkin.getBytes();
        }
    }
    /* -------------------------------- */



    /* --------------------------------
     * Check if the player has a skin
     * If the skin file does not exist,
     * save the skin to a new skin file along with time of saving.
     * -------------------------------- */
    public static void saveSkinToCache(Player player) {
        Path path = Paths.get(SKIN_CACHE_DIR, player.getUniqueId() + ".skin");
        File skinFile = path.toFile();

        if (skinFile.exists()) {
            debugLog("Skin file already exists for player: " + player.getName() + ", skipping skin save.");
            return;
        }

        debugLog("Skin file does not exist for player: " + player.getName() + ", creating new skin file.");
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, fetchSkinByte(player));
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Failed to cache skin for " + player.getName(), e); // Include exception in log
        } catch (NullPointerException e) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "NullPointerException while caching skin for " + player.getName(), e);
        }
    }
    /* -------------------------------- */

    /* --------------------------------
    * Fetch the skin of the target player from skins directory.
    * This will return a URL link such as https://textures.minecraft.net/texture/d5c4ee5ce20aed9e33e866c66caa37178606234b3721084bf01d13320fb2eb3f
    * -------------------------------- */
    public static String fetchSkinURL(UUID uuid) {
        Path path = Paths.get(SKIN_CACHE_DIR, uuid + ".skin");
        File skinFile = path.toFile();

        if (!skinFile.exists()) {
            debugLog("Skin file does not exist for player: " + uuid + ", fetching steve skin.");
            return config.getString("heads.defaultPlayer");
        }

        try {
            debugLog("Fetched skin URL for player: " + uuid + " from cache.");
            return new String(Files.readAllBytes(path)).trim();
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Failed to read skin file for " + uuid, e); // Include exception in log
            return null;
        }
    }








    /* --------------------------------
     * Get Custom heads.
     * This method creates a custom head item with a given URL and name.
     * -------------------------------- */
    public static ItemStack getIcon(String messagePath, String headURL, String targetName, @Nullable String replaceTitle) {
            return setTarget(getIconHead(messagePath, headURL, targetName, replaceTitle ), targetName);
    }

    public static ItemStack getIcon(String messagePath, String headURL, int targetName, @Nullable String replaceTitle) {
        return setPageNumber(getIconHead(messagePath, headURL, String.valueOf(targetName), replaceTitle ), targetName);
    }

    public static ItemStack getIconHead(String messagePath, String headURL, String targetName, @Nullable String replaceTitle) {
        try {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            PlayerProfile profile = createPlayerProfile(targetName.replace("_", "").length() > 15
                    ? targetName.replace("_", "").substring(0, 15)
                    : targetName.replace("_", ""));
            profile.getTextures().setSkin(new URI(headURL).toURL());
            meta.setOwnerProfile(profile);
            if (replaceTitle != null) {
                meta.setDisplayName(replaceTitle);
            } else {
                meta.setDisplayName(config.getString(messagePath + ".title"));
            }
            if (!config.getStringList(messagePath + ".lore").isEmpty()) {
                meta.setLore(config.getStringList(messagePath + ".lore"));
            }
            head.setItemMeta(meta);

            debugLog("Fetching custom head for: " + targetName + " with URL: " + headURL);

            return setTarget(head, targetName);
        } catch (MalformedURLException | URISyntaxException e) {
            debugLog("Failed to create head for " + targetName + " with URL " + headURL + ": " + e.getMessage());
            return new ItemStack(Material.PLAYER_HEAD);
        }
    }
    /* -------------------------------- */

    public static ItemStack createPlayerShopIcon(String ownerName, UUID ownerUUID, String displayName, @Nullable List<String> lore) {

        ItemStack shopIcon = getIcon("messages.playerShops.buttons",
                fetchSkinURL(ownerUUID),
                ownerUUID.toString(),
                displayName);

        ItemMeta meta = shopIcon.getItemMeta();
        if (lore != null) {
            meta.setLore(lore);
        } else {
            meta.setLore(config.getStringList("messages.playerShops.buttons.default-lore"));
        }
        shopIcon.setItemMeta(meta);

        return shopIcon;
    }


    /* -------------------------------- */


}
