package me._w41k3r.shopkeeperAddon.Economy;

import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import me._w41k3r.shopkeeperAddon.Main;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class PriceChatInput {
    public static HashMap<Player, PriceChatInput> allPriceChats = new HashMap<>();
    public String chat;
    public Player player;
    public ItemStack priceItem;
    public Shopkeeper shopkeeper;
    public PriceChatInput(Player p, ItemStack clickedItem, Shopkeeper keeper)
    {
        this.player = p;
        this.chat = null;
        this.priceItem = clickedItem;
        this.shopkeeper = keeper;
    }
    public void setChat(String chatm)
    {
        this.chat = chatm;
        (new BukkitRunnable(){
            @Override
            public void run() {
                if(chat != null)
                {
                    if(chat.equalsIgnoreCase("cancel")){
                        EcoHandler.onPriceCancel(player, Main.plugin.messages.getString("Trade-Setup-Cancel"), shopkeeper);
                    }
                    else {
                        EcoHandler.onPriceInput(player, chat, priceItem, shopkeeper);
                    }
                    PriceChatInput.allPriceChats.remove(player);

                }
            }
        }).runTask(Main.plugin);

    }
}
