package me.w41k3r.shopkeepersaddon.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static me.w41k3r.shopkeepersaddon.Main.*;

public class SetPriceTask implements Listener {
    private final Player player;
    private final int slot;
    private final PriceInputCallback callback;

    public SetPriceTask(Player player, int slot, PriceInputCallback callback) {
        this.player = player;
        this.slot = slot;
        this.callback = callback;
    }

    public void startEdit() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEditPrice(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(player)) {
            return;
        }

        try {
            if (event.getMessage().equalsIgnoreCase("cancel")) {
                sendPlayerMessage(player, setting().getString("messages.price-change-cancelled"));
                event.setCancelled(true);
                HandlerList.unregisterAll(this);
                return;
            }
            double price = Double.parseDouble(event.getMessage());

            event.setCancelled(true);

            Bukkit.getScheduler().runTask(plugin, () -> {

                callback.onPriceSet(price, slot>17 ? slot-18 : slot, slot);
                HandlerList.unregisterAll(this);
            });

            HandlerList.unregisterAll(this);

        } catch (NumberFormatException e) {
            event.setCancelled(true);
            sendPlayerMessage(player, setting().getString("messages.invalid-price"));
        }

    }
}
