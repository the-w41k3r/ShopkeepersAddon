package me.w41k3r.shopkeepersAddon.economy.events;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.events.PlayerOpenUIEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperOpenUIEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperTradeEvent;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import me.w41k3r.shopkeepersAddon.economy.objects.ShopEditTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.BukkitScheduler;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.logging.Level;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.economy.EconomyManager.hasMoney;
import static me.w41k3r.shopkeepersAddon.economy.PersistantDataManager.getPrice;
import static me.w41k3r.shopkeepersAddon.economy.PersistantDataManager.isEconomyItem;
import static me.w41k3r.shopkeepersAddon.gui.managers.Utils.removeEconomyItem;
import static me.w41k3r.shopkeepersAddon.gui.managers.Utils.setItemsOnTradeSlots;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.registry;

public class EconomyListener implements Listener {


    @EventHandler
    public void OpenEditorUI(ShopkeeperOpenUIEvent event){
        if (!(event.getUIType().equals(ShopkeepersAPI.getDefaultUITypes().getEditorUIType()))
                || !config.getBoolean("economy.enabled")) {
            return;
        }

        debugLog("Shopkeeper opened editor UI" + event.getUIType());

        ShopEditTask shopEditor= new ShopEditTask(event.getPlayer(), event.getShopkeeper());
        shopEditor.startEdit();
    }


    @EventHandler
    public void PlayerOpenUI(PlayerOpenUIEvent event){
        if (event.getUIType().equals(ShopkeepersAPI.getDefaultUITypes().getTradingUIType())) {

        }
    }


    @EventHandler
    public void onTradeSelect(TradeSelectEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> removeEconomyItem((Player) event.getWhoClicked()), 1);
        MerchantRecipe recipe = event.getMerchant().getRecipe(event.getIndex());
        Player player = (Player) event.getWhoClicked();
        if (!isEconomyItem(recipe.getIngredients().get(0))
                && !isEconomyItem(recipe.getResult())){
            BukkitScheduler scheduler = player.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                Bukkit.getScheduler().runTaskLater(plugin, () -> removeEconomyItem(player), 1);
            }, 2L);
            return;
        }
        debugLog(recipe.getIngredients().get(0).getItemMeta().toString());

        if(isEconomyItem(recipe.getIngredients().get(0))){
            debugLog("Checking if player has money for item");
            if (!hasMoney(player, getPrice(event.getMerchant().getRecipe(event.getIndex()).getIngredients().getFirst()))){
                sendPlayerMessage(player, config.getString("messages.noMoney"));
                event.setCancelled(true);
                return;
            }
            BukkitScheduler scheduler = player.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                setItemsOnTradeSlots(event, 0);
            }, 5L);
        }
    }

    @EventHandler
    public void onTrade(ShopkeeperTradeEvent event) {
        TradingRecipe recipe = event.getTradingRecipe();
        if (isEconomyItem(recipe.getResultItem().copy())) {
            event.setCancelled(true);
            return;
        }
        debugLog("Trading now!");
        Player player = event.getPlayer();
        Shopkeeper shopkeeper = event.getShopkeeper();
        boolean isAdminShopkeeper = shopkeeper instanceof AdminShopkeeper;
        Double price;
        if (isEconomyItem(recipe.getItem1().copy())){
            price = getPrice(recipe.getItem1().copy());
            Money.withdrawPlayer(player, price);
            if (!isAdminShopkeeper){
                // Hacky Fix here temporary
                String shopkeeperId = String.valueOf(shopkeeper.getId());
                File dataFile = new File(plugin.getDataFolder().getParentFile(), "Shopkeepers/data/save.yml");
                YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
                String ownerUUID = dataConfig.getString(shopkeeperId + ".owner uuid");
                Money.depositPlayer(ownerUUID, price);
            }
        }

    }

    @EventHandler
    public void onTrade2(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() == null
                || event.getCurrentItem() == null
                || event.getCurrentItem().equals(Material.AIR)
                || ShopkeepersAPI.getUIRegistry().getUISession(player) == null
        ) {
            return;
        }

        if (event.getClickedInventory() instanceof MerchantInventory
                && event.getSlot() == 2
                && ShopkeepersAPI.getUIRegistry().getUISession(player).getUIType() == ShopkeepersAPI.getDefaultUITypes().getTradingUIType()
        ) {
            debugLog("Trading Shopkeeper!");
            MerchantInventory merchantInventory = (MerchantInventory) event.getClickedInventory();
            Shopkeeper shopkeeper = ShopkeepersAPI.getUIRegistry().getUISession(player).getShopkeeper();
            boolean isAdminShopkeeper = shopkeeper instanceof AdminShopkeeper;
            MerchantRecipe recipe = merchantInventory.getSelectedRecipe();
            if (isEconomyItem(recipe.getResult())) {
                Double price = getPrice(recipe.getResult());
                int maxTrades = 1;


                // Hacky Fix here temporary
                String shopkeeperId = String.valueOf(shopkeeper.getId());
                File dataFile = new File(plugin.getDataFolder().getParentFile(), "Shopkeepers/data/save.yml");
                YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
                String ownerUUID = dataConfig.getString(shopkeeperId + ".owner uuid");

                // Check for shift-click
                if (event.isShiftClick()) {
                    int affordableTrades = 64;
                    if (!isAdminShopkeeper){
                        double ownerMoney = Money.getBalance(ownerUUID);
                        affordableTrades = (int) Math.floor(ownerMoney / price);
                    }

                    maxTrades = Math.min(affordableTrades, 64);
                }

                price = price * maxTrades;

                if (!isAdminShopkeeper) {

                    double ownerMoney = Money.getBalance(ownerUUID);
                    if (ownerMoney < price) {
                        sendPlayerMessage(player, config.getString("messages.noMoneyOwner"));
                        event.setCancelled(true);
                        return;
                    }
                    Money.withdrawPlayer(ownerUUID, price);
                    Money.depositPlayer(player, price);
                    ItemStack ingredient = recipe.getIngredients().getFirst().clone();
                    ingredient.setAmount(maxTrades);
                    event.getClickedInventory().removeItem(ingredient);
                    PlayerShopkeeper playerShopkeeper = (PlayerShopkeeper) shopkeeper;
                    Inventory container = ((Chest) playerShopkeeper.getContainer().getState()).getBlockInventory();
                    container.addItem(ingredient);
                    if (recipe.getIngredients().size() > 1) {
                        ItemStack secondIngredient = recipe.getIngredients().getLast().clone();
                        secondIngredient.setAmount(maxTrades);
                        event.getClickedInventory().removeItem(secondIngredient);
                        container.addItem(secondIngredient);
                    }
                } else {
                    Money.depositPlayer(player, price);
                    ItemStack ingredient = recipe.getIngredients().getFirst().clone();
                    ingredient.setAmount(maxTrades);
                    event.getClickedInventory().removeItem(ingredient);
                    if (recipe.getIngredients().size() > 1) {
                        ItemStack secondIngredient = recipe.getIngredients().getLast().clone();
                        secondIngredient.setAmount(maxTrades);
                        event.getClickedInventory().removeItem(secondIngredient);
                    }
                }
            }
        }
    }

    @EventHandler
    public void InventoryClose(InventoryCloseEvent event){
//
//        Player player = (Player) event.getPlayer();
//        if (ShopkeepersAPI.getUIRegistry().getUISession(player) != null
//                && !(ShopkeepersAPI.getUIRegistry().getUISession(player).getUIType() == ShopkeepersAPI.getDefaultUITypes().getEditorUIType())
//        ){
//            for (ItemStack item : event.getInventory().getContents()){
//                if (item != null && isEconomyItem(item)){
//                    event.getInventory().remove(item);
//                    return;
//                }
//            }
//        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> removeEconomyItem((Player) event.getPlayer()), 1);
    }

    @EventHandler
    public void InventoryOpen(InventoryOpenEvent event){
        Player player = (Player) event.getPlayer();
        if (ShopkeepersAPI.getUIRegistry().getUISession(player) == null){
            for (ItemStack item : event.getInventory().getContents()){
                if (item != null && isEconomyItem(item)){
                    event.getInventory().remove(item);
                    return;
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> removeEconomyItem((Player) event.getPlayer()), 1);
    }

    @EventHandler
    public void onClickEconomyItem(InventoryClickEvent event){
        if (isEconomyItem(event.getCurrentItem())
                && ShopkeepersAPI.getUIRegistry().getUISession((Player) event.getWhoClicked()) == null
        ){
            event.setCancelled(true);
            for (ItemStack item : event.getInventory().getContents()){
                if (item != null && isEconomyItem(item)){
                    event.getInventory().remove(item);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onItemPickup(InventoryPickupItemEvent event){
        if (!isEconomyItem(event.getItem().getItemStack())){
            return;
        }
        if (event.getInventory().getHolder() instanceof Player){
            Bukkit.getScheduler().runTaskLater(plugin, () -> removeEconomyItem((Player) event.getInventory().getHolder()), 1);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event){
        if (!isEconomyItem(event.getItemDrop().getItemStack())){
            return;
        }
        event.setCancelled(true);
        Bukkit.getScheduler().runTaskLater(plugin, () -> removeEconomyItem(event.getPlayer()), 1);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event){
        if (!isEconomyItem(event.getEntity().getItemStack())){
            return;
        }
        event.setCancelled(true);

    }


}
