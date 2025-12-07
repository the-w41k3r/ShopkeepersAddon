package me.w41k3r.shopkeepersAddon.gui.listeners;

import me.w41k3r.shopkeepersAddon.ShopkeepersAddon;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyListeners implements Listener {
    @EventHandler
    public void onServiceRegisterEvent(final ServiceRegisterEvent event) {
        if (ShopkeepersAddon.Money != null)
            return;

        final RegisteredServiceProvider<?> provider = event.getProvider();

        if (provider.getService().equals(Economy.class)) {
            ShopkeepersAddon.Money = (Economy) provider;
            ShopkeepersAddon.debugLog("Economy provider set to: " + ShopkeepersAddon.Money.getName());
        }
    }
}
