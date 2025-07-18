package me.w41k3r.shopkeepersAddon.gui.listeners;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.Plugin;

import java.util.List;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.debugLog;
import static me.w41k3r.shopkeepersAddon.economy.PersistantDataManager.isEconomyItem;

public class PacketsHijacking extends PacketAdapter {
    public PacketsHijacking(Plugin plugin, PacketType... types) {
        super(plugin, types);
    }

    @Override
    public void onPacketSending(PacketEvent e){
        debugLog("Fixing max uses for economy items in merchant recipes");
        for(List<MerchantRecipe> recipes : e.getPacket().getMerchantRecipeLists().getValues()){
            for(MerchantRecipe recipe : recipes){
                if(isEconomyItem(recipe.getResult())) {
                    if ((recipe.getMaxUses() == recipe.getUses() || recipe.getMaxUses() == 0)
                        && e.getPlayer().getInventory().containsAtLeast(recipe.getIngredients().get(0), 1)
                        && (recipe.getIngredients().size() < 2 || e.getPlayer().getInventory().containsAtLeast(recipe.getIngredients().get(1), 1))
                    ) {
                        recipe.setMaxUses(1000000000);
                    }
                }
            }
        }
    }
}
