package dev.MrFlyn.shopkeeperNavAddon.Economy;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.events.PlayerOpenUIEvent;
import com.nisovin.shopkeepers.api.events.ShopkeeperTradeEvent;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import com.nisovin.shopkeepers.shopkeeper.SKTradingRecipe;
import com.nisovin.shopkeepers.ui.trading.TradingUIType;
import dev.MrFlyn.shopkeeperNavAddon.InvUtils;
import dev.MrFlyn.shopkeeperNavAddon.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class EcoHandler {
    public static void onTradeSetup(Player p, InventoryClickEvent e){
        if((e.getCurrentItem() == null || !(e.getCurrentItem().isSimilar(e.getCursor())))
                &&e.getSlot()<9&&e.getCursor()!=null&&e.getCursor().getType()!=Material.AIR&&
                !InvUtils.hasPersistentData("ItemPrice",e.getCurrentItem(), PersistentDataType.DOUBLE)){
            e.getClickedInventory().setItem(e.getSlot()+18,
                    InvUtils.ItemBuilder(Material.valueOf(
                            Main.plugin.messages.getString("Currency-Item.Material")), 1,
                            Main.plugin.messages.getString("Currency-Item.Name-Format").replace("[amount]",
                                    Main.plugin.vaultHook.formattedMoney(0.0)),
                            Main.plugin.messages.getStringList("Currency-Item.Lore"),
            "ItemPrice", 0.0));
        }
        else if((e.getCurrentItem() == null || !(e.getCurrentItem().isSimilar(e.getCursor())))
                &&(e.getSlot()>17&&e.getSlot()<27)&&e.getCursor()!=null&&e.getCursor().getType()!=Material.AIR&&
                !InvUtils.hasPersistentData("ItemPrice",e.getCurrentItem(), PersistentDataType.DOUBLE)){
            e.getClickedInventory().setItem(e.getSlot()-18, InvUtils.ItemBuilder(Material.valueOf(
                            Main.plugin.messages.getString("Currency-Item.Material")), 1,
                    Main.plugin.messages.getString("Currency-Item.Name-Format").replace("[amount]",
                            Main.plugin.vaultHook.formattedMoney(0.0)),
                    Main.plugin.messages.getStringList("Currency-Item.Lore"),
                    "ItemPrice", 0.0, Main.plugin.messages.getInt("Currency-Item.CustomModelData")));
        }
        else if(InvUtils.hasPersistentData("ItemPrice", e.getCurrentItem(), PersistentDataType.DOUBLE)){

        }
    }

    public static void onPriceItemClick(Player p, ItemStack clickedItem, Shopkeeper shopkeeper){
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CLOSE_WINDOW);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        p.sendMessage(Main.plugin.messages.getString("Price-Input-Request"));
        PriceChatInput.allPriceChats.put(p, new PriceChatInput(p, clickedItem, shopkeeper));
    }

    public static void onPriceCancel(Player p, String reason, Shopkeeper shopkeeper){
        p.closeInventory();
        shopkeeper.openEditorWindow(p);
        p.sendMessage(reason);
    }

    public static void onPriceSuccess(Player p, Shopkeeper shopkeeper) {
        p.closeInventory();
        shopkeeper.openEditorWindow(p);
        p.sendMessage(Main.plugin.messages.getString("Price-Set-Successfully"));
    }

    public static void onPriceInput(Player p, String priceInput, ItemStack clickedItem, Shopkeeper shopkeeper){
        try {
            double price = Double.parseDouble(priceInput);
            if(price>Main.plugin.getConfig().getDouble("EconomyHook.SellingPriceLimit")){
//                onPriceCancel(p, "§cYour trade setup was cancelled due to invalid price input. Highest price limit is "
//                        +String.format("%.2f",Main.plugin.getConfig().getDouble("SellingPriceLimit")), shopkeeper);
                onPriceCancel(p, Main.plugin.messages.getString("Price-Limit-Reached")
                        .replace("[maxPrice]", String.format("%.2f", Main.plugin.getConfig().getDouble("EconomyHook.SellingPriceLimit"))), shopkeeper);
                return;
            }
            ItemMeta meta = clickedItem.getItemMeta();
//            meta.setDisplayName("Price "+String.format("%.2f",price));
            meta.setDisplayName(Main.plugin.messages.getString("Currency-Item.Name-Format").replace("[amount]",
                    Main.plugin.vaultHook.formattedMoney(price)));
            meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "ItemPrice"), PersistentDataType.DOUBLE, price);
            clickedItem.setItemMeta(meta);
            onPriceSuccess(p, shopkeeper);

        }catch (Exception e){
            onPriceCancel(p, Main.plugin.messages.getString("Invalid-Price-Input"), shopkeeper);
        }
    }

    public static void onTradeSelect(TradeSelectEvent e){
        Player p = (Player) e.getWhoClicked();
        MerchantRecipe recipe = e.getMerchant().getRecipe(e.getIndex());
        ItemStack item1 = recipe.getIngredients().get(0);
        ItemStack item2 = recipe.getIngredients().size() < 2 ? null : recipe.getIngredients().get(1);
        ItemStack result = recipe.getResult();
        Inventory tradeInv = e.getView().getTopInventory();
        if (InvUtils.hasPersistentData("ItemPrice", item1, PersistentDataType.DOUBLE)) {
            double price = InvUtils.getPersistentDataPrice(item1);
            if (Main.plugin.vaultHook.hasMoney(p.getName(), price)) {
                tradeInv.setItem(0, item1);
            }
        }
//        else if(InvUtils.hasPersistentData("ItemPrice", result, PersistentDataType.DOUBLE)){
//            if(p.getInventory().containsAtLeast(item1, item1.getAmount())&&(item2 == null || p.getInventory().containsAtLeast(item2, item2.getAmount()))){
//                tradeInv.setItem(0, item1);
//                tradeInv.setItem(1, item2);
//                tradeInv.setItem(2, result);
//                p.getInventory().removeItem(item1);
//                if(item2!=null)
//                    p.getInventory().removeItem(item2);
//            }
//        }
        p.updateInventory();
    }



//    public static void onTrade(ShopkeeperTradeEvent e){
//        Player p = e.getPlayer();
//
//
//    }

    public static void onTrade(InventoryClickEvent e){
        if(e.getCurrentItem()==null)
            return;
        if(e.getClickedInventory() == null)
            return;
        if(!(e.getClickedInventory() instanceof MerchantInventory))
            return;
        MerchantInventory merchantInv = (MerchantInventory) e.getClickedInventory();
        if(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex())==null) {
//            System.out.println("MERCHANT NULL");
            return;
        }
        if(ShopkeepersAPI.getUIRegistry().getUISession((Player) e.getWhoClicked())==null)
            return;
        if(ShopkeepersAPI.getUIRegistry().getUISession((Player) e.getWhoClicked()).getUIType() != TradingUIType.INSTANCE)
            return;
        if(e.getSlot()!=2)
            return;
        Player p = (Player) e.getWhoClicked();
        if(p.getInventory().firstEmpty()==-1){
            p.sendMessage(Main.plugin.messages.getString("Player-Inventory-Full"));
            p.closeInventory();
            return;
        }

        if(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()) == null)
            return;

        ItemStack item1 = e.getClickedInventory().getItem(0);
        ItemStack item2 = e.getClickedInventory().getItem(1);
        Shopkeeper keeper = ShopkeepersAPI.getUIRegistry().getUISession(p).getShopkeeper();
        if((InvUtils.hasPersistentData("ItemPrice",item1,PersistentDataType.DOUBLE)
                ||InvUtils.hasPersistentData("ItemPrice",item2,PersistentDataType.DOUBLE))&&
                !InvUtils.hasPersistentData("ItemPrice",e.getClickedInventory().getItem(2), PersistentDataType.DOUBLE)){
            e.setCancelled(true);
            if(keeper instanceof PlayerShopkeeper){
                Container c1 = (Container) ((PlayerShopkeeper) keeper).getContainer().getState();
                if(!c1.getInventory().containsAtLeast(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getResult(), merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getResult().getAmount())) {
                    p.sendMessage(Main.plugin.messages.getString("Shop-Out-Of-Stock"));
                    return;
                }
                if (item2 != null) {
                    HashMap<Integer, ItemStack> left = c1.getInventory().addItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(1));
                    if(left.isEmpty()){
                        ItemStack resultClone = merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(1).clone();
                        int addedAmount = resultClone.getAmount();
                        resultClone.setAmount(addedAmount);
                        c1.getInventory().removeItem(resultClone);
                    }
                    if(!left.isEmpty()){
                        ItemStack resultClone = merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(1).clone();
                        int addedAmount = resultClone.getAmount()-left.get(0).getAmount();
                        resultClone.setAmount(addedAmount);
                        c1.getInventory().removeItem(resultClone);
                        p.sendMessage(Main.plugin.messages.getString("Supply-Chest-Is-Full"));
                        p.closeInventory();
                        return;
                    }

                }
            }
            double price = InvUtils.getPersistentDataPrice(item1);
            if(!Main.plugin.vaultHook.hasMoney(p.getName(), price)) {
                p.closeInventory();
                p.sendMessage(Main.plugin.messages.getString("Player-Out-Of-Money"));
                return;
            }
            Main.plugin.vaultHook.takeMoney(p.getName(), price);
            if(!Main.plugin.vaultHook.hasMoney(p.getName(), price)){
                e.getClickedInventory().setItem(0, null);
                p.updateInventory();
            }
            if(keeper instanceof PlayerShopkeeper){
                PlayerShopkeeper ps = (PlayerShopkeeper) keeper;
                Main.plugin.vaultHook.giveMoney(ps.getOwnerName(), price);
                Container c = (Container) ps.getContainer().getState();
                c.getInventory().removeItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getResult());
//                c.getBlockInventory().addItem(item1);
                if(item2!=null) {
                    merchantInv.removeItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(1));
                    c.getInventory().addItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(1));
                }
//                InvUtils.removeItems(e.getTradingRecipe().getResultItem().getType(), e.getTradingRecipe().getResultItem().getAmount(), c.getBlockInventory());
                p.getInventory().addItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getResult());

                if(Bukkit.getServer().getOnlinePlayers().contains(ps.getOwner())){
                    ps.getOwner().sendMessage(Main.plugin.messages.getString("Owner-Add-Money").replace("[amount]", String.format("%.2f", price)));
                }
                p.updateInventory();
            }
            else if (keeper instanceof AdminShopkeeper) {
                AdminShopkeeper as = (AdminShopkeeper) keeper;
//                System.out.println(merchantInv.getSelectedRecipeIndex()+"RESULT");
                p.getInventory().addItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getResult());
                if(item2!=null) {
                    merchantInv.removeItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(1));
//                    System.out.println("REMOVED");
                }
                p.updateInventory();
            }

        }
        else if (InvUtils.hasPersistentData("ItemPrice", e.getClickedInventory().getItem(2), PersistentDataType.DOUBLE) &&
                !(InvUtils.hasPersistentData("ItemPrice", e.getClickedInventory().getItem(0), PersistentDataType.DOUBLE)
                        || InvUtils.hasPersistentData("ItemPrice", e.getClickedInventory().getItem(1), PersistentDataType.DOUBLE))) {
            e.setCancelled(true);

            double price = InvUtils.getPersistentDataPrice(e.getCurrentItem());
            if (keeper instanceof PlayerShopkeeper) {
                PlayerShopkeeper ps = (PlayerShopkeeper) keeper;
                Container chest = (Container) ps.getContainer().getState();
                if (!Main.plugin.vaultHook.hasMoney(ps.getOwnerName(), price)) {
                    p.closeInventory();
                    p.sendMessage(Main.plugin.messages.getString("Shop-Out-Of-Money"));
                    return;
                }
                HashMap<Integer, ItemStack> left1 = chest.getInventory().addItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(0));
                if(left1.isEmpty()){
                    ItemStack resultClone = merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(0).clone();
                    int addedAmount = resultClone.getAmount();
                    resultClone.setAmount(addedAmount);
                    chest.getInventory().removeItem(resultClone);
                }
                if(!left1.isEmpty()){
                    ItemStack resultClone = merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(0).clone();
                    int addedAmount = resultClone.getAmount()-left1.get(0).getAmount();
                    resultClone.setAmount(addedAmount);
                    chest.getInventory().removeItem(resultClone);
                    p.sendMessage(Main.plugin.messages.getString("Supply-Chest-Is-Full"));
                    p.closeInventory();
                    return;
                }
                if (item2 != null) {
                    HashMap<Integer, ItemStack> left = chest.getInventory().addItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(1));
                    if(left.isEmpty()){
                        ItemStack resultClone = merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(1).clone();
                        int addedAmount = resultClone.getAmount();
                        resultClone.setAmount(addedAmount);
                        chest.getInventory().removeItem(resultClone);
                    }
                    if(!left.isEmpty()){
                        ItemStack resultClone = merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(1).clone();
                        int addedAmount = resultClone.getAmount()-left.get(0).getAmount();
                        resultClone.setAmount(addedAmount);
                        chest.getInventory().removeItem(resultClone);
                        p.sendMessage(Main.plugin.messages.getString("Supply-Chest-Is-Full"));
                        p.closeInventory();
                        return;
                    }

                }

                    chest.getInventory().addItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(0));
                    if(e.getClickedInventory().getItem(1)!=null)
                        chest.getInventory().addItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(1));
                Main.plugin.vaultHook.takeMoney(ps.getOwnerName(), price);
                if (Bukkit.getServer().getOnlinePlayers().contains(ps.getOwner())) {
//                    ps.getOwner().sendMessage("§a-" + String.format("%.2f", price));
                    ps.getOwner().sendMessage(Main.plugin.messages.getString("Owner-Subtract-Money").replace("[amount]", String.format("%.2f", price)));
                }

                merchantInv.removeItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(0));
                if(item2!=null)
                    merchantInv.removeItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(1));
//                e.getClickedInventory().setItem(0, null);
//                e.getClickedInventory().setItem(1, null);
                Main.plugin.vaultHook.giveMoney(p.getName(), price);
                e.setCurrentItem(null);
                p.updateInventory();
            } else if (keeper instanceof AdminShopkeeper) {
                AdminShopkeeper as = (AdminShopkeeper) keeper;
                Main.plugin.vaultHook.giveMoney(p.getName(), price);
                merchantInv.removeItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(0));
                if(item2!=null)
                    merchantInv.removeItem(merchantInv.getMerchant().getRecipe(merchantInv.getSelectedRecipeIndex()).getIngredients().get(1));
//                e.getClickedInventory().setItem(0, null);
//                e.getClickedInventory().setItem(1, null);
                e.setCurrentItem(null);
                p.updateInventory();
            }

        }
    }

}
