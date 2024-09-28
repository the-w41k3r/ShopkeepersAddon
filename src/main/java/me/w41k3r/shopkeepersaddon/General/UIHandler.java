package me.w41k3r.shopkeepersaddon.General;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static me.w41k3r.shopkeepersaddon.General.Utils.*;
import static me.w41k3r.shopkeepersaddon.Main.virtualOwner;

public class UIHandler {

    public static Inventory HomePage = Bukkit.createInventory(virtualOwner, 27, configData("messages.shops.shops-ui"));
    static Inventory adminShopTypes = Bukkit.createInventory(virtualOwner, 27, configData("messages.shops.admin-shops-ui"));
    static Inventory playerShopTypes = Bukkit.createInventory(virtualOwner, 27, configData("messages.shops.player-shops-ui"));




    static Set<ItemStack> adminItemsList = new HashSet<>();
    static List<Inventory> adminShops = new ArrayList<>();
    static List<Inventory> adminItems = new ArrayList<>();
    static Set<ItemStack> adminHeads = new HashSet<>();
    static HashMap<ItemStack, List<Inventory>> adminShopItems = new HashMap<>();

    static Set<ItemStack> playerItemsList = new HashSet<>();
    static List<Inventory> playerShops = new ArrayList<>();
    static List<Inventory> playerItems = new ArrayList<>();
    static Set<ItemStack> playerHeads = new HashSet<>();
    static HashMap<ItemStack, List<Inventory>> playerShopItems = new HashMap<>();

    static void readyItemsUI(FileConfiguration shopData, String shopID) {
        HashSet<ItemStack> items = new HashSet<>();

        String shopPath = shopID + ".offers";
        if (!shopData.contains(shopPath)) {
            shopPath = shopID + ".recipes";
        }

        ConfigurationSection recipesSection = shopData.getConfigurationSection(shopPath);
        if (recipesSection == null) {
            errorLog("No recipes or offers found for shop ID: " + shopID);
            return;
        }

        for (String offerKey : recipesSection.getKeys(false)) {
            String resultItemPath = shopPath + "." + offerKey + ".resultItem";
            ItemStack resultItem = shopData.getItemStack(resultItemPath);
            if (resultItem == null) {
                errorLog("No result item found for shop ID: " + shopID + " and offer key: " + offerKey);
                continue;
            }
            resultItem.setAmount(1);
            items.add(resultItem);
        }

        switch (shopData.getString(shopID + ".type")) {
            case "admin":
                Inventory adminItemsPage = getOrCreatePage(adminItems, "admin-items");
                for (ItemStack item : items) {
                    if (adminItemsList.contains(item)) {
                        continue;
                    }
                    adminItemsPage.addItem(item);
                    adminItemsList.add(item);
                }
                adminItems.set(adminItems.size() - 1, adminItemsPage);
                break;
            default:
                Inventory playerItemsPage = getOrCreatePage(playerItems, "player-items");
                for (ItemStack item : items) {
                    if (playerItemsList.contains(item)) {
                        continue;
                    }
                    playerItemsPage.addItem(item);
                    playerItemsList.add(item);
                }
                playerItems.set(playerItems.size() - 1, playerItemsPage);
                break;
        }

        readyHeadsUI(items, shopData.getConfigurationSection(shopID));
    }


    static void readyHeadsUI(Set<ItemStack> items, ConfigurationSection shopkeeper) {
        ItemStack head;
        switch (shopkeeper.getString("type")) {
            case "admin":
                String type = shopkeeper.getConfigurationSection("object").getString("type");
                head = getIcon(shopkeeper.getString("uniqueId"), "adminshop", type);
                ItemMeta adminHeadMeta = head.getItemMeta();
                List<String> adminLore = new ArrayList<>();
                for (ItemStack item : items) {
                    adminLore.add(item.getType().toString());
                }
                adminHeadMeta.setLore(adminLore);
                head.setItemMeta(adminHeadMeta);
                if (adminHeads.contains(head)) {
                    return;
                }
                adminHeads.add(head);
                Inventory adminHeadsPage = getOrCreatePage(adminShops, "admin-shops");
                adminHeadsPage.addItem(head);
                adminShops.set(adminShops.size() - 1, adminHeadsPage);
                break;

            default:
                head = getIcon(shopkeeper.getString("uniqueId"), "playershop", "player");
                ItemMeta playerHeadMeta = head.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add(getShopTitle(shopkeeper.getString("owner")));
                for (ItemStack item : items) {
                    lore.add(item.getType().toString());
                }
                playerHeadMeta.setLore(lore);
                head.setItemMeta(playerHeadMeta);
                if (playerHeads.contains(head)) {
                    return;
                }
                playerHeads.add(head);
                Inventory playerHeadsPage = getOrCreatePage(playerShops, "player-shops");
                playerHeadsPage.addItem(head);
                playerShops.set(playerShops.size() - 1, playerHeadsPage);
                break;
        }

        readyItemSellerUIs(shopkeeper, items, head);
    }

    static void readyItemSellerUIs(ConfigurationSection shopkeeper, Set<ItemStack> items, ItemStack head) {
        switch (shopkeeper.getString("type")) {
            case "admin":
                for (ItemStack item : items) {
                    if (!adminShopItems.containsKey(item)) {
                        List<Inventory> invList = new ArrayList<>();
                        Inventory inv = getOrCreatePage(adminShopItems.get(item), "admin-sellers");
                        inv.setItem(47, item);
                        invList.add(inv);
                        adminShopItems.put(item, invList);
                    }
                    for (Inventory inventory : adminShopItems.get(item)) {
                        if (inventory.contains(head)) {
                            break;
                        } else if (inventory.firstEmpty() == -1) {
                            continue;
                        }
                        inventory.addItem(head);
                    }
                }
                break;
            default:
                for (ItemStack item : items) {
                    if (!playerShopItems.containsKey(item)) {
                        List<Inventory> invList = new ArrayList<>();
                        Inventory inv = getOrCreatePage(playerShopItems.get(item), "player-sellers");
                        inv.setItem(47, item);
                        invList.add(inv);
                        playerShopItems.put(item, invList);
                    }
                    for (Inventory inventory : playerShopItems.get(item)) {
                        if (inventory.contains(head)) {
                            break;
                        } else if (inventory.firstEmpty() == -1) {
                            continue;
                        }
                        inventory.addItem(head);
                    }
                }
                break;
        }


    }





    static Inventory getOrCreatePage(List<Inventory> ShopsUI, String type) {
        if (ShopsUI == null){
            ShopsUI = new ArrayList<>();
        }
        int currentPage = ShopsUI.size() - 1;
        Inventory page = currentPage >= 0 ? ShopsUI.get(currentPage) : null;

        if (page == null || page.firstEmpty() == -1) {
            if (page != null) {
                addNextPageButton(ShopsUI, currentPage);
            }
            currentPage++;
            page = createPage(type, currentPage);
            ShopsUI.add(page);
        }
        return page;
    }

    private static Inventory createPage(String type, int currentPage) {
        Inventory page;
        String title = configData("messages.shops." + type + "-ui") + " - Page " + (currentPage + 1);
        page = Bukkit.createInventory(virtualOwner, 54, title);
        setLastRowAsStainedGlassPane(page, currentPage, type);
        return page;
    }

    private static void setLastRowAsStainedGlassPane(Inventory inventory, int currentPage, String type) {
        ItemStack stainedGlassPane = createStainedGlassPane();
        for (int i = 45; i < 54; i++) {
            if (i == 45 && currentPage > 0) {
                inventory.setItem(i, createPreviousPageButton());
            } else if (i == 49) {
                inventory.setItem(i, createCloseInventoryButton(currentPage, type));
            } else {
                inventory.setItem(i, stainedGlassPane);
            }
        }
    }

    private static ItemStack createStainedGlassPane() {
        ItemStack stainedGlassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta stainedGlassPaneMeta = stainedGlassPane.getItemMeta();
        stainedGlassPaneMeta.setDisplayName(" ");
        stainedGlassPane.setItemMeta(stainedGlassPaneMeta);
        return stainedGlassPane;
    }

    private static ItemStack createPreviousPageButton() {
                ItemStack previousPageButton = getCustomHead("PreviousPage", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjllYTFkODYyNDdmNGFmMzUxZWQxODY2YmNhNmEzMDQwYTA2YzY4MTc3Yzc4ZTQyMzE2YTEwOThlNjBmYjdkMyJ9fX0=");
                ItemMeta previousPageButtonMeta = previousPageButton.getItemMeta();
                previousPageButtonMeta.setDisplayName(configData("messages.previous-page-button"));
                previousPageButton.setItemMeta(previousPageButtonMeta);
        return previousPageButton;
            }

    private static ItemStack createCloseInventoryButton(int currentPage, String type) {
                ItemStack closeInventoryButton = new ItemStack(Material.BARRIER, 1);
        ItemMeta meta = closeInventoryButton.getItemMeta();
        meta.setDisplayName(configData("messages.close-inventory-button"));
        meta = setData(meta, "closeInventory", "true");
        meta = setData(meta, "currentPage", String.valueOf(currentPage));
        meta = setData(meta, "inventoryType", type);
        closeInventoryButton.setItemMeta(meta);
        return closeInventoryButton;
    }


    static void addNextPageButton(List<Inventory> ShopsUI, int currentPage) {
        ItemStack nextPageButton = getCustomHead("NextPage", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI3MWE0NzEwNDQ5NWUzNTdjM2U4ZTgwZjUxMWE5ZjEwMmIwNzAwY2E5Yjg4ZTg4Yjc5NWQzM2ZmMjAxMDVlYiJ9fX0=");
        ItemMeta meta = nextPageButton.getItemMeta();
        meta.setDisplayName(configData("messages.next-page-button"));
        nextPageButton.setItemMeta(meta);
        ShopsUI.get(currentPage).setItem(53, nextPageButton);
    }

    static void createShopsUI() {
        ItemStack playerShopIcon = getCustomHead("PlayerSelUI", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTFiZTRiNWI1OTJmZWUyMWE2NWUwZjkwMzAzOGM1MzMzYmUzODgyMzRhNDM3MzFkNGFkZmU1ZDU3ZDM2NDRlNSJ9fX0=");
        ItemMeta playerMeta = playerShopIcon.getItemMeta();
        playerMeta.setDisplayName(configData("messages.shops.player-shops-ui"));
        playerShopIcon.setItemMeta(playerMeta);
        HomePage.setItem(11, playerShopIcon);

        ItemStack adminShopIcon = getCustomHead("AdminSelUI", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjI3ODQzMDdiODkyZjUyYjkyZjc0ZmE5ZGI0OTg0YzRmMGYwMmViODFjNjc1MmU1ZWJhNjlhZDY3ODU4NDI3ZSJ9fX0=");
        ItemMeta adminMeta = adminShopIcon.getItemMeta();
        adminMeta.setDisplayName(configData("messages.shops.admin-shops-ui"));
        adminShopIcon.setItemMeta(adminMeta);
        HomePage.setItem(15, adminShopIcon);
    }


    static void selectionUIs() {
        ItemStack playerShopsIcon = getCustomHead("PlayerShopsUI", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYThjZGRjZmY0NDBlMjc5MmQ5OTE0ZTFkZDExN2ZhODJiZDg1OWIyOWVkMzlkZGRlMzQ1MzU0Mzg2YjAyYjc3NiJ9fX0=");
        ItemMeta playerShopsMeta = playerShopsIcon.getItemMeta();
        playerShopsMeta.setDisplayName(configData("messages.shops.player-shops-ui"));
        playerShopsIcon.setItemMeta(playerShopsMeta);
        playerShopTypes.setItem(11, playerShopsIcon);

        ItemStack playerItemsIcon = getCustomHead("PlayerItemsUI", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODU0Nzg0ZmI0NzM0Nzg0NzQzYmYzMDI2NDlmYjU3MDAxNzFlYWY2Njc4NjFhYjJiMjVjZTlhZWU3YTEzODE2MCJ9fX0=");
        ItemMeta playerItemsMeta = playerItemsIcon.getItemMeta();
        playerItemsMeta.setDisplayName(configData("messages.shops.player-items-ui"));
        playerItemsIcon.setItemMeta(playerItemsMeta);
        playerShopTypes.setItem(15, playerItemsIcon);

        ItemStack adminShopsIcon = getCustomHead("AdminShopsUI", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFmNzE0MTMzY2U3OGQxMTgxYzRkNWQzZTUzNzExZWNlMTBjNGM5YTI4MjAxMTg4ZWUxYTZmMzVjYzBmYTNjYSJ9fX0=");
        ItemMeta adminShopsMeta = adminShopsIcon.getItemMeta();
        adminShopsMeta.setDisplayName(configData("messages.shops.admin-shops-ui"));
        adminShopsIcon.setItemMeta(adminShopsMeta);
        adminShopTypes.setItem(11, adminShopsIcon);

        ItemStack adminItemsIcon = getCustomHead("AdminItemsUI", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODU0Nzg0ZmI0NzM0Nzg0NzQzYmYzMDI2NDlmYjU3MDAxNzFlYWY2Njc4NjFhYjJiMjVjZTlhZWU3YTEzODE2MCJ9fX0=");
        ItemMeta adminItemsMeta = adminItemsIcon.getItemMeta();
        adminItemsMeta.setDisplayName(configData("messages.shops.admin-items-ui"));
        adminItemsIcon.setItemMeta(adminItemsMeta);
        adminShopTypes.setItem(15, adminItemsIcon);
    }



}



