package me._w41k3r.shopkeepersAddon.GlobalShopGui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShopInv implements InventoryHolder {
    private MenuType menuType;
    private MenuType previousMenu;
    private Inventory inventory;
    private List<ItemStack> stackList;
    public ShopInv(MenuType Type, int size, String title) {
        this.inventory = Bukkit.createInventory(this, size, title);
        this.menuType = Type;
        this.stackList = new ArrayList<>();
    }
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public MenuType getMenuType() {
        return this.menuType;
    }

    public MenuType getPreviousMenuType() {
        return this.previousMenu;
    }

    public void setPreviousMenuType(MenuType mt) {
        this.previousMenu = mt;
    }

    public void setCachedPageItems(List<ItemStack> stacksList){
        this.stackList = stacksList;
    }

    public List<ItemStack> getCachedPageItems(){
        return stackList;
    }


}
