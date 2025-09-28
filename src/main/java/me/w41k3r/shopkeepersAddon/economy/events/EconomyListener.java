package me.w41k3r.shopkeepersAddon.economy.events;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.events.ShopkeeperOpenUIEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperTradeEvent;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import me.w41k3r.shopkeepersAddon.economy.objects.ShopEditTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.economy.EconomyManager.formatPrice;
import static me.w41k3r.shopkeepersAddon.economy.EconomyManager.hasMoney;
import static me.w41k3r.shopkeepersAddon.economy.PersistantDataManager.getPrice;
import static me.w41k3r.shopkeepersAddon.economy.PersistantDataManager.isEconomyItem;
import static me.w41k3r.shopkeepersAddon.gui.managers.Utils.removeEconomyItem;
import static me.w41k3r.shopkeepersAddon.gui.managers.Utils.setItemsOnTradeSlots;

public class EconomyListener implements Listener {

    private static final int REMOVE_ECONOMY_ITEM_DELAY = 1;
    private static final String SHOPKEEPERS_DATA_PATH = "Shopkeepers/data/save.yml";
    private static final String OWNER_UUID_PATH = ".owner uuid";
    // Added throttling to prevent multiple rapid fire events
    private final Map<UUID, Long> lastTradeTime = new ConcurrentHashMap<>();
    private static final long TRADE_COOLDOWN_MS = 100; // 100ms cooldown between trades
    private final Map<UUID, BulkTradeInfo> bulkTrades = new ConcurrentHashMap<>();


    @EventHandler
    public void onOpenEditorUI(ShopkeeperOpenUIEvent event) {
        if (!config.getBoolean("economy.enabled", false) ||
                !event.getUIType().equals(ShopkeepersAPI.getDefaultUITypes().getEditorUIType())) {
            return;
        }

        debugLog("Shopkeeper opened editor UI: " + event.getUIType());
        ShopEditTask shopEditor = new ShopEditTask(event.getPlayer(), event.getShopkeeper());
        shopEditor.startEdit();
    }

    @EventHandler
    public void onTradeSelect(TradeSelectEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        scheduleRemoveEconomyItem(player);

        MerchantRecipe recipe = event.getMerchant().getRecipe(event.getIndex());
        if (recipe == null || recipe.getIngredients().isEmpty()) return;

        ItemStack firstIngredient = recipe.getIngredients().get(0);
        if (!isEconomyItem(firstIngredient) && !isEconomyItem(recipe.getResult())) {
            scheduleRemoveEconomyItem(player);
            return;
        }

        debugLog("Recipe ingredient meta: " + (firstIngredient.hasItemMeta() ? firstIngredient.getItemMeta().toString() : "No meta"));

        if (isEconomyItem(firstIngredient)) {
            if (!hasMoney(player, getPrice(firstIngredient))) {
                sendPlayerMessage(player, config.getString("messages.noMoney", "You don't have enough money!"));
                event.setCancelled(true);
                return;
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> setItemsOnTradeSlots(event, 0), 5L);
        }
    }

    @EventHandler
    public void onShopkeeperTrade(ShopkeeperTradeEvent event) {
        TradingRecipe recipe = event.getTradingRecipe();
        if (isEconomyItem(recipe.getResultItem().copy())) {
            event.setCancelled(true);
            debugLog("Cancelled trade with economy item as result.");
            return;
        }
        debugLog("Trading now!");

        debugLog("Processing shopkeeper trade!");
        Player player = event.getPlayer();
        Shopkeeper shopkeeper = event.getShopkeeper();


        if (event.getClickEvent().isShiftClick()){
            event.setCancelled(true);
            processBulkTrade(player, shopkeeper, recipe, event.getClickEvent().getClickedInventory().getItem(0).getAmount());
            return;
        }

        if (isEconomyItem(recipe.getItem1().copy())) {
            processEconomyTrade(player, shopkeeper, recipe);
        }
    }

    private void processEconomyTrade(Player player, Shopkeeper shopkeeper, TradingRecipe recipe) {
        double price = getPrice(recipe.getItem1().copy());
        Money.withdrawPlayer(player, price);

        if (!(shopkeeper instanceof AdminShopkeeper)) {
            depositToShopOwner(shopkeeper, price);
        }
        sendPlayerMessage(player, config.getString("messages.buySuccess", "§aYou have purchased %item% for %price%.")
                .replace("%item%", recipe.getResultItem().getItemMeta().getDisplayName().isEmpty() ? recipe.getResultItem().getType().name() : recipe.getResultItem().getItemMeta().getDisplayName())
                .replace("%price%", formatPrice(price)));
    }

    private void processBulkTrade(Player player, Shopkeeper shopkeeper, TradingRecipe recipe, int tradeCount) {
        double pricePerTrade = getPrice(recipe.getItem1().copy());
        double totalPrice = pricePerTrade * tradeCount;

        if (!hasMoney(player, totalPrice)) {
            sendPlayerMessage(player, config.getString("messages.noMoney", "You don't have enough money!"));
            return;
        }

        Money.withdrawPlayer(player, totalPrice);


        ItemStack resultItem = recipe.getResultItem().copy();
        resultItem.setAmount(tradeCount*resultItem.getAmount());

        player.getInventory().addItem(resultItem);
        if (!(shopkeeper instanceof AdminShopkeeper)) {
            PlayerShopkeeper playerShopkeeper = (PlayerShopkeeper) shopkeeper;
            Container container = playerShopkeeper.getContainer().getState() instanceof Container cont ? cont : null;
            if (container != null) {
                Inventory inv = container.getInventory();
                inv.removeItem(resultItem);
            }
            depositToShopOwner(shopkeeper, totalPrice);

        }

        sendPlayerMessage(player, config.getString("messages.buySuccess", "§aYou have purchased %item% for %price%.")
                .replace("%item%", recipe.getResultItem().getItemMeta().getDisplayName().isEmpty() ? recipe.getResultItem().getType().name() : recipe.getResultItem().getItemMeta().getDisplayName())
                .replace("%price%", formatPrice(totalPrice)));
    }


    private void depositToShopOwner(Shopkeeper shopkeeper, double price) {
        try {
            String shopkeeperId = String.valueOf(shopkeeper.getId());
            File dataFile = new File(plugin.getDataFolder().getParentFile(), SHOPKEEPERS_DATA_PATH);
            YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
            String ownerUUID = dataConfig.getString(shopkeeperId + OWNER_UUID_PATH);

            if (ownerUUID != null) {
                Money.depositPlayer(ownerUUID, price);
            } else {
                debugLog("Could not find owner UUID for shopkeeper: " + shopkeeperId);
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to deposit money to shop owner", e);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (isTradingUISlot(event, player)) {
            handleTradingUIClick(event, player);
        } else if (isEconomyItemClick(event, player)) {
            handleEconomyItemClick(event);
        }
    }

    private boolean isTradingUISlot(InventoryClickEvent event, Player player) {
        return event.getClickedInventory() instanceof MerchantInventory &&
                event.getSlot() == 2 &&
                ShopkeepersAPI.getUIRegistry().getUISession(player) != null &&
                ShopkeepersAPI.getUIRegistry().getUISession(player).getUIType() ==
                        ShopkeepersAPI.getDefaultUITypes().getTradingUIType();
    }

    private boolean isEconomyItemClick(InventoryClickEvent event, Player player) {
        return isEconomyItem(event.getCurrentItem()) &&
                ShopkeepersAPI.getUIRegistry().getUISession(player) == null;
    }

    private void handleTradingUIClick(InventoryClickEvent event, Player player) {
        MerchantInventory merchantInventory = (MerchantInventory) event.getClickedInventory();
        MerchantRecipe recipe = merchantInventory.getSelectedRecipe();

        if (recipe == null || recipe.getIngredients().isEmpty() || recipe.getResult() == null) {
            return;
        }

        if (isEconomyItem(recipe.getResult())) {
            processEconomyTradeClick(event, player, recipe);
        }
    }

    private void processEconomyTradeClick(InventoryClickEvent event, Player player, MerchantRecipe recipe) {
        Shopkeeper shopkeeper = ShopkeepersAPI.getUIRegistry().getUISession(player).getShopkeeper();
        boolean isAdminShopkeeper = shopkeeper instanceof AdminShopkeeper;
        double pricePerTrade = getPrice(recipe.getResult());

        int maxTrades = calculateMaxTrades(event, recipe, shopkeeper, pricePerTrade);
        if (maxTrades <= 0) {
            event.setCancelled(true);
            return;
        }

        double totalPrice = pricePerTrade * maxTrades;

        if (isAdminShopkeeper) {
            handleAdminTrade(event, recipe, player, maxTrades, totalPrice);
        } else {
            handlePlayerShopTrade(event, recipe, player, shopkeeper, maxTrades, totalPrice);
        }
        sendPlayerMessage(player, config.getString("messages.sellSuccess", "§aYou have sold %item% for %price%.")
                .replace("%item%", recipe.getIngredients().getFirst().getItemMeta().getDisplayName().isEmpty() ? recipe.getIngredients().getFirst().getType().name() : recipe.getIngredients().getFirst().getItemMeta().getDisplayName())
                .replace("%price%", formatPrice(totalPrice)));

    }

    private int calculateMaxTrades(InventoryClickEvent event, MerchantRecipe recipe, Shopkeeper shopkeeper, double pricePerTrade) {
        int maxTrades = 1;

        if (event.isShiftClick()) {
            maxTrades = calculateShiftClickTrades(event, recipe, shopkeeper, pricePerTrade);
        }

        return Math.min(maxTrades, 64); // Cap at stack size
    }

    private int calculateShiftClickTrades(InventoryClickEvent event, MerchantRecipe recipe, Shopkeeper shopkeeper, double pricePerTrade) {
        int maxTradesByItems = calculateMaxTradesByIngredients(event.getClickedInventory(), recipe);

        if (shopkeeper instanceof AdminShopkeeper) {
            return maxTradesByItems;
        }

        double ownerMoney = getOwnerMoney(shopkeeper);
        int affordableTrades = (int) Math.floor(ownerMoney / pricePerTrade);

        return Math.min(maxTradesByItems, affordableTrades);
    }

    private int calculateMaxTradesByIngredients(Inventory inventory, MerchantRecipe recipe) {
        int maxTrades = Integer.MAX_VALUE;

        for (ItemStack ingredient : recipe.getIngredients()) {
            if (ingredient == null || ingredient.getType() == Material.AIR) continue;

            int required = ingredient.getAmount();
            int available = countSimilarItems(inventory, ingredient);
            maxTrades = Math.min(maxTrades, available / required);
        }

        return maxTrades;
    }

    private int countSimilarItems(Inventory inventory, ItemStack target) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.isSimilar(target)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private double getOwnerMoney(Shopkeeper shopkeeper) {
        try {
            String shopkeeperId = String.valueOf(shopkeeper.getId());
            File dataFile = new File(plugin.getDataFolder().getParentFile(), SHOPKEEPERS_DATA_PATH);
            YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
            String ownerUUID = dataConfig.getString(shopkeeperId + OWNER_UUID_PATH);

            return ownerUUID != null ? Money.getBalance(ownerUUID) : 0;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to get owner money for shopkeeper: " + shopkeeper.getId(), e);
            return 0;
        }
    }

    private void handleAdminTrade(InventoryClickEvent event, MerchantRecipe recipe, Player player, int maxTrades, double totalPrice) {
        Money.depositPlayer(player, totalPrice);
        removeIngredients(event.getClickedInventory(), recipe, maxTrades);
    }

    private void handlePlayerShopTrade(InventoryClickEvent event, MerchantRecipe recipe, Player player, Shopkeeper shopkeeper, int maxTrades, double totalPrice) {
        double ownerMoney = getOwnerMoney(shopkeeper);
        if (ownerMoney < totalPrice) {
            sendPlayerMessage(player, config.getString("messages.noMoneyOwner", "The shop owner doesn't have enough money!"));
            event.setCancelled(true);
            return;
        }

        // Transfer money
        String ownerUUID = getOwnerUUID(shopkeeper);
        if (ownerUUID != null) {
            Money.withdrawPlayer(ownerUUID, totalPrice);
            Money.depositPlayer(player, totalPrice);
        }

        // Handle items
        removeIngredients(event.getClickedInventory(), recipe, maxTrades);
        depositIngredientsToContainer(shopkeeper, recipe, maxTrades);
    }

    private String getOwnerUUID(Shopkeeper shopkeeper) {
        try {
            String shopkeeperId = String.valueOf(shopkeeper.getId());
            File dataFile = new File(plugin.getDataFolder().getParentFile(), SHOPKEEPERS_DATA_PATH);
            YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
            return dataConfig.getString(shopkeeperId + OWNER_UUID_PATH);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to get owner UUID for shopkeeper: " + shopkeeper.getId(), e);
            return null;
        }
    }

    private void removeIngredients(Inventory inventory, MerchantRecipe recipe, int multiplier) {
        for (ItemStack ingredient : recipe.getIngredients()) {
            if (ingredient == null || ingredient.getType() == Material.AIR) continue;

            ItemStack toRemove = ingredient.clone();
            toRemove.setAmount(ingredient.getAmount() * multiplier);
            inventory.removeItem(toRemove);
        }
    }

    private void depositIngredientsToContainer(Shopkeeper shopkeeper, MerchantRecipe recipe, int multiplier) {
        if (!(shopkeeper instanceof PlayerShopkeeper playerShopkeeper)) return;

        try {
            Inventory container = ((Chest) playerShopkeeper.getContainer().getState()).getBlockInventory();

            for (ItemStack ingredient : recipe.getIngredients()) {
                if (ingredient == null || ingredient.getType() == Material.AIR) continue;

                ItemStack toAdd = ingredient.clone();
                toAdd.setAmount(ingredient.getAmount() * multiplier);
                container.addItem(toAdd);
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to deposit ingredients to shop container", e);
        }
    }

    private void handleEconomyItemClick(InventoryClickEvent event) {
        event.setCancelled(true);
        removeEconomyItemsFromInventory(event.getInventory());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            scheduleRemoveEconomyItem(player);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        if (ShopkeepersAPI.getUIRegistry().getUISession(player) == null) {
            removeEconomyItemsFromInventory(event.getInventory());
        }
        scheduleRemoveEconomyItem(player);
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (!isEconomyItem(event.getItem().getItemStack())) return;

        if (event.getInventory().getHolder() instanceof Player player) {
            scheduleRemoveEconomyItem(player);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!isEconomyItem(event.getItemDrop().getItemStack())) return;

        event.setCancelled(true);
        scheduleRemoveEconomyItem(event.getPlayer());
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (isEconomyItem(event.getEntity().getItemStack())) {
            event.setCancelled(true);
        }
    }

    private void scheduleRemoveEconomyItem(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> removeEconomyItem(player), REMOVE_ECONOMY_ITEM_DELAY);
    }

    private void removeEconomyItemsFromInventory(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && isEconomyItem(item)) {
                inventory.remove(item);
                break; // Remove one at a time, will be called again if needed
            }
        }
    }
}