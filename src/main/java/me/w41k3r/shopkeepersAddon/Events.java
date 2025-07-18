package me.w41k3r.shopkeepersAddon;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import me.w41k3r.shopkeepersAddon.economy.events.EconomyListener;
import me.w41k3r.shopkeepersAddon.gui.listeners.GUIListeners;
import me.w41k3r.shopkeepersAddon.gui.listeners.PacketsHijacking;
import me.w41k3r.shopkeepersAddon.gui.listeners.UpdateListeners;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.plugin;
import static me.w41k3r.shopkeepersAddon.gui.managers.SkinsManager.*;

public class Events implements Listener {

    public static void initEvents() {
        plugin.getServer().getPluginManager().registerEvents(new Events(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new GUIListeners(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EconomyListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new UpdateListeners(), plugin);
//        PacketEvents.getAPI().getEventManager().registerListener(
//                new PacketsHijacking(), PacketListenerPriority.NORMAL);
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketsHijacking(plugin,
                PacketType.Play.Server.OPEN_WINDOW_MERCHANT));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        saveSkinToCache(event.getPlayer());
    }

}
