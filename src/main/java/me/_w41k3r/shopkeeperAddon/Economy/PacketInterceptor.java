package me._w41k3r.shopkeeperAddon.Economy;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me._w41k3r.shopkeeperAddon.InvUtils;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class PacketInterceptor extends PacketAdapter {
    public PacketInterceptor(Plugin plugin, PacketType... types) {
        super(plugin, types);
    }

    @Override
    public void onPacketReceiving(PacketEvent e){
        if(e.getPacketType()==PacketType.Play.Client.CLOSE_WINDOW||e.getPacketType()== PacketType.Play.Client.WINDOW_CLICK) {
            if (PriceChatInput.allPriceChats.containsKey(e.getPlayer())) {
                e.setCancelled(true);
            }
        }
    }
    @Override
    public void onPacketSending(PacketEvent e){
        for(List<MerchantRecipe> recipes : e.getPacket().getMerchantRecipeLists().getValues()){
            for(MerchantRecipe recipe : recipes){
                if(InvUtils.hasPersistentData("ItemPrice", recipe.getResult(), PersistentDataType.DOUBLE)) {
                    if (recipe.getMaxUses() == recipe.getUses() || recipe.getMaxUses() == 0) {
                        recipe.setMaxUses(1000000000);
                    }
                }
            }
        }
    }
}
