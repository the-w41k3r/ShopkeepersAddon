package me.w41k3r.shopkeepersAddon.economy.objects;

import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.economy.EconomyManager.formatPrice;
import static me.w41k3r.shopkeepersAddon.economy.EconomyManager.getCurrencyItem;
import static me.w41k3r.shopkeepersAddon.economy.PersistantDataManager.isEconomyItem;
import static me.w41k3r.shopkeepersAddon.gui.managers.Utils.removeEconomyItem;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.blacklistedItems;

public class ShopEditTask implements Listener, PriceInputCallback {

    private final Player player;
    private final Shopkeeper shopkeeper;
    private static int page = 1;

    private static final Material GRAY_PANE = Material.GRAY_STAINED_GLASS_PANE;
    private static final Material BARRIER = Material.BARRIER;
    private static final int PAGE_SLOT = 31;
    private static final int NAVIGATION_SLOT = 35;

    public ShopEditTask(Player player, Shopkeeper shopkeeper) {
        this.player = player;
        this.shopkeeper = shopkeeper;
    }

    public void startEdit(){
        debugLog("Blacklisted items: " + blacklistedItems);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEditShop(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player) || event.getClickedInventory() == null) {
            return;
        }

        if (handleBlacklistedItem(event)) return;
        if (handleEconomyItem(event)) return;
        handlePriceSlots(event);
    }

    private boolean handleBlacklistedItem(InventoryClickEvent event) {
        if (event.getCurrentItem() != null &&
                event.getCursor() != null &&
                blacklistedItems.contains(event.getCursor().getType().name()) &&
                shopkeeper instanceof PlayerShopkeeper) {

            sendPlayerMessage(player, config.getString("messages.blacklistedItem", "This item is blacklisted!"));
            scheduleTask(() -> event.getClickedInventory().setItem(event.getSlot(), new ItemStack(BARRIER)));
            event.setCancelled(true);
            return true;
        }
        return false;
    }

    private boolean handleEconomyItem(InventoryClickEvent event) {
        if (!isEconomyItem(event.getCurrentItem())) return false;

        debugLog(event.getCursor().toString());
        if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
            event.setCancelled(true);
            scheduleTask(() -> {
                event.getClickedInventory().setItem(event.getSlot(), event.getCursor());
                player.getItemOnCursor().setAmount(0);
            });
            return true;
        }

        page = getCurrentPage(event.getClickedInventory());
        SetPriceTask setPriceTask = new SetPriceTask(player, event.getSlot(), this);
        setPriceTask.startEdit();
        sendPlayerMessage(player, config.getString("messages.setPrice"));
        player.closeInventory();
        return true;
    }

    private void handlePriceSlots(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null && !currentItem.getType().equals(GRAY_PANE)) {
            return;
        }

        int slot = event.getRawSlot();
        if ((slot < 9) || (slot > 17 && slot < 27)) {
            handlePriceItemSlot(event, slot);
        }
    }

    private void handlePriceItemSlot(InventoryClickEvent event, int slot) {
        int targetSlot = slot < 9 ? slot + 18 : slot - 18;
        ItemStack targetItem = event.getClickedInventory().getItem(targetSlot);

        if (isInvalidItem(targetItem)) {
            int delay = shopkeeper instanceof PlayerShopkeeper ? 2 : 0;
            scheduleTask(() -> {
                debugLog("Setting default price item at slot " + targetSlot);
                setDefaultPriceItem(event.getClickedInventory(), targetSlot);
            }, delay);
        }
    }

    private boolean isInvalidItem(ItemStack item) {
        return item == null || item.getType().isAir() || item.getType().equals(GRAY_PANE);
    }

    private int getCurrentPage(Inventory inventory) {
        ItemStack pageItem = inventory.getItem(PAGE_SLOT);
        return pageItem != null ? pageItem.getAmount() : 1;
    }

    private void scheduleTask(Runnable task) {
        scheduleTask(task, 1);
    }

    private void scheduleTask(Runnable task, int delay) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    private void setDefaultPriceItem(Inventory inventory, int slot) {
        ItemStack item = getCurrencyItem(1.0, slot < 9);
        inventory.setItem(slot, item);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getPlayer().equals(player)) {
            return;
        }

        debugLog("Inventory closed! " + ShopkeepersAPI.getUIRegistry().getUISession(player));
        if (ShopkeepersAPI.getUIRegistry().getUISession(player) != null) {
            HandlerList.unregisterAll(this);
            scheduleTask(() -> removeEconomyItem(player));
        }
    }

    @Override
    public void onPriceSet(double price, int rawSlot) {
        sendPlayerMessage(player, config.getString("messages.priceChanged").replace("%price%", formatPrice(price)));
        shopkeeper.openEditorWindow(player);

        navigateToCorrectPage();

        ItemStack priceItem = getCurrencyItem(price, rawSlot < 9);
        player.getOpenInventory().getTopInventory().setItem(rawSlot, priceItem);
    }

    private void navigateToCorrectPage() {
        InventoryView openInventory = player.getOpenInventory();
        Inventory topInventory = openInventory.getTopInventory();

        int currentPage = getCurrentPage(topInventory);
        int clicksNeeded = page - currentPage;

        if (clicksNeeded != 0) {
            for (int i = 0; i < Math.abs(clicksNeeded); i++) {
                InventoryClickEvent clickEvent = new InventoryClickEvent(
                        openInventory,
                        InventoryType.SlotType.CONTAINER,
                        NAVIGATION_SLOT,
                        clicksNeeded > 0 ? ClickType.RIGHT : ClickType.LEFT,
                        null
                );
                Bukkit.getPluginManager().callEvent(clickEvent);
            }
        }
    }
}