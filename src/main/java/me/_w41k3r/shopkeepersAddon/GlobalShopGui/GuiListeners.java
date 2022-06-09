package me._w41k3r.shopkeepersAddon.GlobalShopGui;


import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.events.ShopkeeperEditedEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperRemoveEvent;
import me._w41k3r.shopkeepersAddon.Economy.Log;
import me._w41k3r.shopkeepersAddon.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.stream.Collectors;

public class GuiListeners implements Listener {
    public boolean firstJoin = true;
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        if(firstJoin){
            ShopkeeperSorter.updateSellingItemStacks(e.getPlayer());
            ShopkeeperSorter.updateVisualRepresentationOfShopkeepers(e.getPlayer());
            ShopkeeperSorter.updateShopkeeperItemstacks(e.getPlayer());
            firstJoin = false;
        }
    }
    @EventHandler
    public void onShopkeeperEdit(ShopkeeperEditedEvent e){
        Player p = e.getPlayer();
            ShopkeeperSorter.updateSellingItemStacks(p);

//            for (Shopkeeper s : ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getAllPlayerShopkeepers()) {
//                for (TradingRecipe tr : s.getTradingRecipes(p)) {
//                    ItemStack stack = tr.getResultItem().copy();
//                    stack.setAmount(1);
//                    if (!sellingItemStacks.contains(stack)) {
//                        sellingItemStacks.add(stack);
//                    }
//                }
//            }
//            sellingItemStacks.sort(new ItemStackComparator());
            ShopkeeperSorter.updateVisualRepresentationOfShopkeepers(p);
            ShopkeeperSorter.updateShopkeeperItemstacks(p);
    }
    @EventHandler
    public void onShopkeeperDelete(ShopkeeperRemoveEvent e){
        if(Bukkit.getServer().getOnlinePlayers().size()>0) {
            Player p = Bukkit.getServer().getOnlinePlayers().stream().findAny().get();
            ShopkeeperSorter.updateSellingItemStacks(p);
            ShopkeeperSorter.updateVisualRepresentationOfShopkeepers(p);
            ShopkeeperSorter.updateShopkeeperItemstacks(p);
        }
        firstJoin = true;
    }

//----------------------------------------------------
    private boolean ignoreCancelledEvent(InventoryInteractEvent event) {
        if (event.isCancelled()) {
            Log.debug("  Ignoring already cancelled event.");
            return true;
        }
        return false;
    }

    private boolean ignoreNonMerchantInventory(Inventory inventory) {
        if (!(inventory instanceof MerchantInventory)) {
            Log.debug("  Ignoring non-merchant inventory.");
            return true;
        }
        return false;
    }

    private boolean isInventoryActionIgnored(InventoryAction action) {
        switch (action) {
            case PICKUP_ALL:
            case PICKUP_SOME:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case DROP_ALL_CURSOR:
            case DROP_ONE_CURSOR:
            case DROP_ALL_SLOT:
            case DROP_ONE_SLOT:
            case MOVE_TO_OTHER_INVENTORY:
            case HOTBAR_MOVE_AND_READD:
            case CLONE_STACK:
            case COLLECT_TO_CURSOR:
                return true;
            case NOTHING: // No effect, so there is no harm in canceling it just in case
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
            case SWAP_WITH_CURSOR:
            case HOTBAR_SWAP:
            case UNKNOWN:
            default:
                // Intentionally: If new inventory actions are added to Minecraft, they are initially
                // cancelled by default and need to be explicitly allowed.
                return false;
        }
    }






    //----------------------------------------------
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        HumanEntity whoClicked = e.getWhoClicked();
        Log.debug(() -> "Inventory click: whoClicked=" + whoClicked.getName()
                + ", inventoryType=" + e.getClickedInventory().getType()
                + ", rawSlot=" + e.getRawSlot()
                + ", slotType=" + e.getSlotType()
                + ", action=" + e.getAction()
                + ", cancelled=" + e.isCancelled());

        if (ignoreCancelledEvent(e)) {
            return;
        }

        if (ignoreNonMerchantInventory(e.getClickedInventory())) {
            return;
        }

        // Ignore interactions with non-input slots:
        if (e.getSlotType() != InventoryType.SlotType.CRAFTING) {
            Log.debug("  Ignoring non-input slot click.");
            return;
        }

        // Ignore certain inventory actions (e.g. pick ups):
        if (isInventoryActionIgnored(e.getAction())) {
            Log.debug("  Ignoring inventory action.");
            return;
        }

        // TODO Ignore if a trading recipe is already explicitly selected?


        if(e.getClickedInventory() == null)
        {

            return;
        }
        if(e.getClickedInventory().getHolder() == null)
        {
            return;
        }
        if(e.getCurrentItem() == null)
        {
            return;
        }


        if(e.getView().getTopInventory().getHolder() instanceof ShopInv) {
            e.setCancelled(true);
        }
        if(e.getClickedInventory().getHolder() instanceof ShopInv){
            ShopInv holder = (ShopInv) e.getClickedInventory().getHolder();
            e.setCancelled(true);
            switch (holder.getMenuType()){
                case MAIN_MENU:
                    if(e.getCurrentItem().getType() != Material.PLAYER_HEAD) {
                        return;
                    }
                    if(e.getCurrentItem().getItemMeta().getDisplayName().equals("Item Shop")){
                        (new BukkitRunnable(){
                            @Override
                            public void run() {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.ITEM_SHOP);
                            }
                        }).runTask(ShopkeepersPlugin.getInstance());
                        return;
                    }
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Admin Shop")) {
                        (new BukkitRunnable() {
                            @Override
                            public void run() {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.ADMIN_SHOP);
                            }
                        }).runTask(ShopkeepersPlugin.getInstance());
                        return;
                    }
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Player Shop")) {
                        (new BukkitRunnable() {
                            @Override
                            public void run() {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.PLAYER_SHOP);
                            }
                        }).runTask(ShopkeepersPlugin.getInstance());
                        return;
                    }
                    break;
                case PLAYER_SHOP:
                    if(e.getCurrentItem().getType() != Material.PLAYER_HEAD) {
                        return;
                    }
                    if (e.getSlot() == 51) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.nextPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.PLAYER_SHOP);
                                }
                            }).runTask(ShopkeepersPlugin.getInstance());
                        }
                    } else if (e.getSlot() == 47) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Main Menu")) {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.MAIN_MENU);
                                return;
                            }
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.previousPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.PLAYER_SHOP);
                                }
                            }).runTask(ShopkeepersPlugin.getInstance());
                        }
                    }
                    else {
                        (new BukkitRunnable(){
                            @Override
                            public void run() {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.SHOPKEEPERS, MenuType.PLAYER_SHOP,
                                        e.getCurrentItem().getItemMeta().getDisplayName());
                            }
                        }).runTask(ShopkeepersPlugin.getInstance());
                    }
                    break;
                case ITEM_SHOP:
                    if (e.getSlot() == 51) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.nextPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.ITEM_SHOP);
                                }
                            }).runTask(ShopkeepersPlugin.getInstance());
                        }
                    } else if (e.getSlot() == 47) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Main Menu")) {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.MAIN_MENU);
                                return;
                            }
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.previousPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.ITEM_SHOP);
                                }
                            }).runTask(ShopkeepersPlugin.getInstance());
                        }
                    }
                    else if (e.getSlot()>8&&e.getSlot()<45) {
                        (new BukkitRunnable(){
                            @Override
                            public void run() {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.SHOPKEEPERS, MenuType.ITEM_SHOP, e.getCurrentItem());
                            }
                        }).runTask(ShopkeepersPlugin.getInstance());
                    }
                    break;
                case SHOPKEEPERS:
                    if (e.getSlot() == 51) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD){
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.nextPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.SHOPKEEPERS);
                                }
                            }).runTask(ShopkeepersPlugin.getInstance());
                        }
                    } else if (e.getSlot() == 47) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Main Menu")) {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.MAIN_MENU);
                                return;
                            }if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Item Shop")) {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.ITEM_SHOP);
                                return;
                            }if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Player Shop")) {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.PLAYER_SHOP);
                                return;
                            }
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.previousPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.SHOPKEEPERS);
                                }
                            }).runTask(ShopkeepersPlugin.getInstance());
                        }
                    }
                    else {
                        NamespacedKey key = new NamespacedKey(Main.plugin, "shopLocation");
                        ItemMeta itemMeta = e.getCurrentItem().getItemMeta();
                        PersistentDataContainer tagContainer = itemMeta.getPersistentDataContainer();

                        if(tagContainer.has(key , PersistentDataType.STRING)) {
                            String locString = tagContainer.get(key , PersistentDataType.STRING);
                            Location loc = new Location(Bukkit.getWorld(locString.split(" ")[0]), Double.parseDouble(locString.split(" ")[1]),
                                    Double.parseDouble(locString.split(" ")[2]), Double.parseDouble(locString.split(" ")[3]));
                            if (Main.plugin.getConfig().getBoolean("AllowTeleportToShopkeepers")) {
                                if (e.getWhoClicked().hasPermission("SNA.teleport")) {
                                    if (Main.plugin.plotSquaredHook == null) {
                                        if (Main.plugin.isSafeLocation(loc)) {
                                            e.getWhoClicked().teleport(loc);
                                        } else {
                                            e.getWhoClicked().sendMessage("§cUnsafe location detected. Cancelling teleport...");
                                        }
                                        e.getWhoClicked().closeInventory();
                                    } else {
                                        (new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                Main.plugin.plotSquaredHook.teleportPlayerPlotSquared((Player) e.getWhoClicked(), loc);
                                            }
                                        }).runTask(Main.plugin);
                                    }
                                } else {
                                    e.getWhoClicked().closeInventory();
                                    e.getWhoClicked().sendMessage("§cYou do not have the permission to teleport.");
                                }
                            }
                        }
                    }
                    break;
                case ADMIN_SHOP:
                    if (e.getSlot() == 51) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD){
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.nextPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.ADMIN_SHOP);
                                }
                            }).runTask(ShopkeepersPlugin.getInstance());
                        }
                    } else if (e.getSlot() == 47) {
                        if(e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Main Menu")) {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.MAIN_MENU);
                                return;
                            }
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.previousPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.ADMIN_SHOP);
                                }
                            }).runTask(ShopkeepersPlugin.getInstance());
                        }
                    }
                    else {
                        NamespacedKey key = new NamespacedKey(Main.plugin, "shopLocation");
                        ItemMeta itemMeta = e.getCurrentItem().getItemMeta();
                        PersistentDataContainer tagContainer = itemMeta.getPersistentDataContainer();

                        if (tagContainer.has(key , PersistentDataType.STRING)) {
                            String locString = tagContainer.get(key , PersistentDataType.STRING);
                            Location loc = new Location(Bukkit.getWorld(locString.split(" ")[0]), Double.parseDouble(locString.split(" ")[1]),
                                    Double.parseDouble(locString.split(" ")[2]), Double.parseDouble(locString.split(" ")[3]));
                            if (Main.plugin.getConfig().getBoolean("AllowTeleportToShopkeepers")) {
                                if (e.getWhoClicked().hasPermission("SNA.teleport")) {
                                    if (Main.plugin.plotSquaredHook == null) {
                                        if (Main.plugin.isSafeLocation(loc)) {
                                            e.getWhoClicked().teleport(loc);
                                        } else {
                                            e.getWhoClicked().sendMessage("§cUnsafe location detected. Cancelling teleport...");
                                        }
                                        e.getWhoClicked().closeInventory();
                                    } else {
                                        (new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                Main.plugin.plotSquaredHook.teleportPlayerPlotSquared((Player) e.getWhoClicked(), loc);
                                            }
                                        }).runTask(Main.plugin);
                                    }
                                } else {
                                    e.getWhoClicked().closeInventory();
                                    e.getWhoClicked().sendMessage("§cYou do not have the permission to teleport.");
                                }
                            }
                        }
                    }
                    break;
                case REMOTE_ADMIN_SHOP:
                    if (e.getSlot() == 51) {
                        if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.nextPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.REMOTE_ADMIN_SHOP);
                                }
                            }).runTask(ShopkeepersPlugin.getInstance());
                        }
                    } else if (e.getSlot() == 47) {
                        if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                            if (e.getCurrentItem().getItemMeta().getDisplayName().equals("Main Menu")) {
                                ShopGUI.openShopGUI((Player) e.getWhoClicked(), MenuType.MAIN_MENU);
                                return;
                            }
                            (new BukkitRunnable() {
                                @Override
                                public void run() {
                                    PagingCalculations.previousPage(e.getClickedInventory(), (Player) e.getWhoClicked(), MenuType.REMOTE_ADMIN_SHOP);
                                }
                            }).runTask(ShopkeepersPlugin.getInstance());
                        }
                    } else {
                        NamespacedKey key = new NamespacedKey(Main.plugin, "shopLocation");
                        ItemMeta itemMeta = e.getCurrentItem().getItemMeta();
                        PersistentDataContainer tagContainer = itemMeta.getPersistentDataContainer();

                        if (tagContainer.has(key, PersistentDataType.STRING)) {
                            String locString = tagContainer.get(key, PersistentDataType.STRING);
                            Location loc = new Location(Bukkit.getWorld(locString.split(" ")[0]), Double.parseDouble(locString.split(" ")[1]),
                                    Double.parseDouble(locString.split(" ")[2]), Double.parseDouble(locString.split(" ")[3]));
                            int id = Integer.parseInt(locString.split(" ")[4]);
                            //getShopkeeper lol
                            ShopkeepersAPI.getShopkeeperRegistry().getShopkeeperById(id).openTradingWindow((Player) e.getWhoClicked());
                        }

                    }
                    break;

            }
        }


        cancel(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        Log.debug(() -> "Inventory dragging: whoClicked=" + whoClicked.getName()
                + ", inventoryType=" + event.getInventory().getType()
                + ", rawSlots=" + event.getRawSlots().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"))
                + ", cancelled=" + event.isCancelled());

        if (ignoreCancelledEvent(event)) {
            return;
        }

        if (ignoreNonMerchantInventory(event.getInventory())) {
            return;
        }

        // Ignore interactions with non-input slots:
        if (!containsInputSlot(event.getView(), event.getRawSlots())) {
            Log.debug(() -> "  Ignoring non-input slot drag.");
            return;
        }

        // TODO Ignore if a trading recipe is already explicitly selected?


        cancel(event);
    }

    private void cancel(InventoryInteractEvent event) {
        event.setCancelled(true);
        Log.debug("  Cancelled!");
    }



    private boolean containsInputSlot(InventoryView inventoryView, Set<Integer> rawSlots) {
        for (int rawSlot : rawSlots) {
            InventoryType.SlotType slotType = inventoryView.getSlotType(rawSlot);
            if (slotType == InventoryType.SlotType.CRAFTING) {
                return true;
            }
        }
        return false;
    }
}
