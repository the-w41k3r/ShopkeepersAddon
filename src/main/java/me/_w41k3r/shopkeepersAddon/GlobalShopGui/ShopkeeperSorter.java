package me._w41k3r.shopkeepersAddon.GlobalShopGui;


import com.nisovin.shopkeepers.api.ShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import com.nisovin.shopkeepers.api.shopkeeper.admin.AdminShopType;
import com.nisovin.shopkeepers.api.shopkeeper.player.PlayerShopkeeper;
import me._w41k3r.shopkeepersAddon.InvUtils;
import me._w41k3r.shopkeepersAddon.Main;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ShopkeeperSorter {
    public static ArrayList<ItemStack> sellingItemStacks = new ArrayList<>();
    public static HashMap<Shopkeeper, ItemStack> visualRepresentation = new HashMap<>();
    public static HashMap<ItemStack, ArrayList<Shopkeeper>> shopkeeperItemStacks = new HashMap<>();
    public static List<Shopkeeper> getAllAdminShopkeepers(){
        List<Shopkeeper> adminShops = new ArrayList<>();
        for(Shopkeeper s :ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getAllShopkeepers()){
            if(s.getType() instanceof AdminShopType){
                adminShops.add(s);
            }
        }
        return adminShops;
    }
    public static void updateSellingItemStacks(Player p){
        for (Shopkeeper s : ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getAllPlayerShopkeepers()) {
            for (TradingRecipe tr : s.getTradingRecipes(p)) {
                ItemStack stack = tr.getResultItem().copy();
                stack.setAmount(1);
                if (!sellingItemStacks.contains(stack)) {
                    sellingItemStacks.add(stack);
                }
            }
        }
        sellingItemStacks.sort(new ItemStackComparator());
    }

    public static List<ItemStack> getSortedResultItemStacks(Player p){
//        List<ItemStack> stacks = new ArrayList<>();
//        for (Shopkeeper s : ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getAllPlayerShopkeepers()) {
//            for (TradingRecipe tr : s.getTradingRecipes(p)) {
//                ItemStack stack = tr.getResultItem().copy();
//                stack.setAmount(1);
//                if (!stacks.contains(stack)) {
//                    stacks.add(stack);
//                }
//            }
//        }
        return sellingItemStacks;
    }
    public static List<Shopkeeper> getShopkeepersSellingItemStack(ItemStack itemStack, Player p){
        List<Shopkeeper> keepers = new ArrayList<>();
        itemStack.setAmount(1);
        if(shopkeeperItemStacks.containsKey(itemStack))
            return shopkeeperItemStacks.get(itemStack);
        return keepers;
    }

    public static void updateShopkeeperItemstacks(Player p) {
        for (Shopkeeper s : ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getAllPlayerShopkeepers()) {
            for (TradingRecipe tr : s.getTradingRecipes(p)) {
                ItemStack resultItem = tr.getResultItem().copy();
                resultItem.setAmount(1);
                if (shopkeeperItemStacks.containsKey(resultItem)) {
                    if (!shopkeeperItemStacks.get(resultItem).contains(s))
                        shopkeeperItemStacks.get(resultItem).add(s);
                }
                else {
                    shopkeeperItemStacks.put(resultItem, new ArrayList<Shopkeeper>(Collections.singletonList(s)));
                }
            }
        }
    }
    public static List<ItemStack> getVisualRepresentationOfShopkeepers(List<Shopkeeper> keepers, Player p, ItemStack prioritizedItemResult){
        //&l2x&8[&bOAK_PLANK&8] &f&l+ &7&l2x&8[&bIRON_INGOT&8] &f&l↣ &7&l5x&8[&aNETHERITE_BLOCK&8]
        List<ItemStack> stacks = new ArrayList<>();
        for(Shopkeeper keeper : keepers){
            if(visualRepresentation.containsKey(keeper))
                stacks.add(visualRepresentation.get(keeper));
        }
        return stacks;
//        for(Shopkeeper keeper : keepers){
//            List<String> lore = new ArrayList<>();
//            List<String> prioritizedLore = new ArrayList<>();
//            for(TradingRecipe tr : keeper.getTradingRecipes(p)){
//                String loreLine;
//                String resultItem = (tr.getResultItem().hasItemMeta())?((!tr.getResultItem().getItemMeta().getDisplayName().equals(""))?
//                        tr.getResultItem().getItemMeta().getDisplayName():tr.getResultItem().getType().toString()):tr.getResultItem().getType().toString();
//                String Item1 = (tr.getItem1().hasItemMeta())?((!tr.getItem1().getItemMeta().getDisplayName().equals(""))?
//                        tr.getItem1().getItemMeta().getDisplayName():tr.getItem1().getType().toString()):tr.getItem1().getType().toString();
//                String Item2 = (tr.hasItem2())?((tr.getItem2().hasItemMeta())?((!tr.getItem2().getItemMeta().getDisplayName().equals(""))?
//                        tr.getItem2().getItemMeta().getDisplayName():tr.getItem2().getType().toString()):tr.getItem2().getType().toString()):null;
//                loreLine = "§7§l"+tr.getItem1().getAmount()+"x§8[§b"+Item1+"§8]"+((Item2!=null)?
//                        " §f§l+ §7§l"+tr.getItem2().getAmount()+"x§8[§b"+Item2+"§8]":"")+" §f§l↣ §7§l"+tr.getResultItem().getAmount()+"x§8[§a"+resultItem+"§8]";
//                if(tr.getResultItem().copy().isSimilar(prioritizedItemResult)){
//                    prioritizedLore.add(loreLine);
//                }
//                else {
//                    lore.add(loreLine);
//                }
//            }
//
//            List<String> finalizedLore = new ArrayList<>();
//            finalizedLore.add("Owner Name: "+((keeper instanceof PlayerShopkeeper)?((PlayerShopkeeper)keeper).getOwnerName():"ADMIN SHOP"));
//            finalizedLore.add(" ");
//            prioritizedLore.addAll(lore);
//            if(!prioritizedLore.isEmpty()){
//                finalizedLore.addAll(prioritizedLore.subList(0, Math.min(prioritizedLore.size(), 10)));
//            }
//
//            finalizedLore.addAll(Arrays.asList("...  "," ", "Location: "+keeper.getLocation().getBlockX()+" "+keeper.getLocation().getBlockY()+" "+keeper.getLocation().getBlockZ()));
//            ItemStack stack = InvUtils.customPlayerHead(Main.plugin.keeperHeads.get(0), finalizedLore, (keeper.getName().equals(""))?"Shopkeeper": keeper.getName());
//            NamespacedKey key = new NamespacedKey(Main.plugin, "shopLocation");
//            ItemMeta itemMeta = stack.getItemMeta();
//            itemMeta.getCustomTagContainer().setCustomTag(key, ItemTagType.STRING, keeper.getLocation().getWorld().getName()+" "+
//                    keeper.getLocation().getBlockX()+" "+keeper.getLocation().getBlockY()
//                    +" "+keeper.getLocation().getBlockZ());
//            stack.setItemMeta(itemMeta);
//            stacks.add(stack);
//            Collections.shuffle(Main.plugin.keeperHeads);
//        }
//        return stacks;
    }
    public static List<ItemStack> getVisualRepresentationOfShopkeepers(List<Shopkeeper> keepers, Player p){
        List<ItemStack> stacks = new ArrayList<>();
        for(Shopkeeper keeper : keepers){
            if(visualRepresentation.containsKey(keeper))
                stacks.add(visualRepresentation.get(keeper));
        }
        return stacks;
//        for(Shopkeeper keeper : keepers){
//            List<String> lore = new ArrayList<>();
//            for(TradingRecipe tr : keeper.getTradingRecipes(p)){
//                String loreLine;
//                String resultItem = (tr.getResultItem().hasItemMeta())?((!tr.getResultItem().getItemMeta().getDisplayName().equals(""))?
//                        tr.getResultItem().getItemMeta().getDisplayName():tr.getResultItem().getType().toString()):tr.getResultItem().getType().toString();
//                String Item1 = (tr.getItem1().hasItemMeta())?((!tr.getItem1().getItemMeta().getDisplayName().equals(""))?
//                        tr.getItem1().getItemMeta().getDisplayName():tr.getItem1().getType().toString()):tr.getItem1().getType().toString();
//                String Item2 = (tr.hasItem2())?((tr.getItem2().hasItemMeta())?((!tr.getItem2().getItemMeta().getDisplayName().equals(""))?
//                        tr.getItem2().getItemMeta().getDisplayName():tr.getItem2().getType().toString()):tr.getItem2().getType().toString()):null;
//                loreLine = "§7§l"+tr.getItem1().getAmount()+"x§8[§b"+Item1+"§8]"+((Item2!=null)?
//                        " §f§l+ §7§l"+tr.getItem2().getAmount()+"x§8[§b"+Item2+"§8]":"")+" §f§l↣ §7§l"+tr.getResultItem().getAmount()+"x§8[§a"+resultItem+"§8]";
//                    lore.add(loreLine);
//            }
//
//            List<String> finalizedLore = new ArrayList<>();
//            finalizedLore.add("Owner Name: "+((keeper instanceof PlayerShopkeeper)?((PlayerShopkeeper)keeper).getOwnerName():"ADMIN SHOP"));
//            finalizedLore.add(" ");
//            if(!lore.isEmpty()){
//                finalizedLore.addAll(lore.subList(0, Math.min(lore.size(), 10)));
//            }
//            finalizedLore.addAll(Arrays.asList("... "," ", "Location: "+keeper.getLocation().getBlockX()+" "+keeper.getLocation().getBlockY()+" "+keeper.getLocation().getBlockZ()));
//            ItemStack stack = InvUtils.customPlayerHead(Main.plugin.keeperHeads.get(0), finalizedLore, (keeper.getName().equals(""))?"Shopkeeper": keeper.getName());
//            NamespacedKey key = new NamespacedKey(Main.plugin, "shopLocation");
//            ItemMeta itemMeta = stack.getItemMeta();
//            itemMeta.getCustomTagContainer().setCustomTag(key, ItemTagType.STRING, keeper.getLocation().getWorld().getName()+" "+
//                    keeper.getLocation().getBlockX()+" "+keeper.getLocation().getBlockY()
//                    +" "+keeper.getLocation().getBlockZ());
//            stack.setItemMeta(itemMeta);
//            stacks.add(stack);
//            Collections.shuffle(Main.plugin.keeperHeads);
//        }
//        return stacks;
    }

    public static void updateVisualRepresentationOfShopkeepers(Player p) {
        visualRepresentation.clear();
        for (Shopkeeper keeper : ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getAllShopkeepers()) {
            List<String> lore = new ArrayList<>();
            if(keeper==null||keeper.getLocation() == null){
                Bukkit.getLogger().warning("Unable to update a shopkeeper in the Gui. Possibly a corrupt shopkeeper?");
                continue;
            }
            for (TradingRecipe tr : keeper.getTradingRecipes(p)) {
                String loreLine;
                String resultItem = (tr.getResultItem().hasItemMeta()) ? ((!tr.getResultItem().getItemMeta().getDisplayName().equals("")) ?
                        tr.getResultItem().getItemMeta().getDisplayName() : tr.getResultItem().getType().toString()) : tr.getResultItem().getType().toString();
                String Item1 = (tr.getItem1().hasItemMeta()) ? ((!tr.getItem1().getItemMeta().getDisplayName().equals("")) ?
                        tr.getItem1().getItemMeta().getDisplayName() : tr.getItem1().getType().toString()) : tr.getItem1().getType().toString();
                String Item2 = (tr.hasItem2()) ? ((tr.getItem2().hasItemMeta()) ? ((!tr.getItem2().getItemMeta().getDisplayName().equals("")) ?
                        tr.getItem2().getItemMeta().getDisplayName() : tr.getItem2().getType().toString()) : tr.getItem2().getType().toString()) : null;
                loreLine = "§7§l" + tr.getItem1().getAmount() + "x§8[§b" + Item1 + "§8] " + ((Item2 != null) ?
                        " §f§l+ §7§l" + tr.getItem2().getAmount() + "x§8[§b" + Item2 + "§8]" : "") + " §f§l↣ §7§l" + tr.getResultItem().getAmount() + "x§8[§a" + resultItem + "§8]";
                lore.add(loreLine);
            }

            List<String> finalizedLore = new ArrayList<>();
            finalizedLore.add("Owner Name: " + ((keeper instanceof PlayerShopkeeper) ? ((PlayerShopkeeper) keeper).getOwnerName() : "ADMIN SHOP"));
            finalizedLore.add(" ");
            if (!lore.isEmpty()) {
                finalizedLore.addAll(lore.subList(0, Math.min(lore.size(), 10)));
            }
            finalizedLore.addAll(Arrays.asList("... ", " ", "Location: " + keeper.getLocation().getBlockX() + " " + keeper.getLocation().getBlockY() + " " + keeper.getLocation().getBlockZ()));
            ItemStack stack = InvUtils.customPlayerHead(Main.plugin.keeperHeads.get(0), finalizedLore, (keeper.getName().equals("")) ? "Shopkeeper" : keeper.getName());
            NamespacedKey key = new NamespacedKey(Main.plugin, "shopLocation");
            ItemMeta itemMeta = stack.getItemMeta();
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, keeper.getLocation().getWorld().getName() + " " +
                    keeper.getLocation().getBlockX() + " " + keeper.getLocation().getBlockY()
                    + " " + keeper.getLocation().getBlockZ()+ " " + keeper.getId());
            stack.setItemMeta(itemMeta);
            visualRepresentation.put(keeper, stack);
            Collections.shuffle(Main.plugin.keeperHeads);
        }

    }
    public static List<String> getAllShopOwners(){
        List<String> owners = new ArrayList<>();
        for(PlayerShopkeeper s :ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getAllPlayerShopkeepers()){
            if(!owners.contains(s.getOwnerName())){
                owners.add(s.getOwnerName());
            }
        }
        return owners;

    }
    public static List<ItemStack> getSortedVisualsOfStringList(List<String> StringList){
        Collections.sort(StringList);
        List<ItemStack> items = new ArrayList<>();
        for(String s : StringList){
            items.add(InvUtils.customPlayerHead(Main.plugin.keeperHeads.get(0),null, s));
            Collections.shuffle(Main.plugin.keeperHeads);
        }
        return items;
    }
    public static List<Shopkeeper> getShopkeepersOwnedByPlayer(String name){
        List<Shopkeeper> keepers = new ArrayList<>();
        for(PlayerShopkeeper s :ShopkeepersPlugin.getInstance().getShopkeeperRegistry().getAllPlayerShopkeepers()){
           if(s.getOwnerName().equals(name)){
               keepers.add(s);
           }
        }
        return keepers;
    }


}
