package me._w41k3r.shopkeepersAddon.PluginHooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private Economy econ = null;
    public VaultHook(){

    }
    public boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    public String formattedMoney(double amount){
        return econ.format(amount);
    }
    public boolean hasMoney(String name, double money){
        return econ.has(name, money);
    }
    public void takeMoney(String name, double money){
        econ.withdrawPlayer(name, money);
    }
    public long getMoneyLong(String name){
        return (long) econ.getBalance(name);
    }

    public void giveMoney(String name, double money) {
        econ.depositPlayer(name, money);
    }
}
