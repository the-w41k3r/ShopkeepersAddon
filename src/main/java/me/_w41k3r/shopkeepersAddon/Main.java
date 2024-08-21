package me._w41k3r.shopkeepersAddon;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import me._w41k3r.shopkeepersAddon.Economy.ConfigHandler;
import me._w41k3r.shopkeepersAddon.Economy.EcoListeners;
import me._w41k3r.shopkeepersAddon.Economy.PacketInterceptor;
import me._w41k3r.shopkeepersAddon.GlobalShopGui.Commands;
import me._w41k3r.shopkeepersAddon.GlobalShopGui.GuiListeners;
import me._w41k3r.shopkeepersAddon.GlobalShopGui.PlayerHeadSkins;
import me._w41k3r.shopkeepersAddon.PluginHooks.PlotSquaredHook;
import me._w41k3r.shopkeepersAddon.PluginHooks.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends JavaPlugin {
    public List<String> keeperHeads =new ArrayList<>();
    public static Main plugin;
    public FileConfiguration messages;
    public PlotSquaredHook plotSquaredHook = null;
    public VaultHook vaultHook = null;


    @Override
    public void onEnable(){
        saveDefaultConfig();
        saveResource("messages.yml", false);
        plugin = this;
        ConfigHandler.load();
        if(getConfig().getBoolean("EconomyHook.Enabled")){
            Bukkit.getLogger().info(String.format("[%s] - Attempting to enable Economy Hook.", getDescription().getName()));
            if(Bukkit.getPluginManager().getPlugin("Vault")==null){
                Bukkit.getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            vaultHook = new VaultHook();
            if(!vaultHook.setupEconomy()){
                Bukkit.getLogger().severe(String.format("[%s] - Please install an Vault compatible economy plugin!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            if(Bukkit.getPluginManager().getPlugin("ProtocolLib")==null){
                Bukkit.getLogger().severe(String.format("[%s] - Please install ProtocolLib!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            Bukkit.getLogger().info(String.format("[%s] - EconomyHooking Success.", getDescription().getName()));
            Bukkit.getPluginManager().registerEvents(new EcoListeners(),this);
            ProtocolLibrary.getProtocolManager().addPacketListener(new PacketInterceptor(this, PacketType.Play.Client.CLOSE_WINDOW,
                    PacketType.Play.Client.WINDOW_CLICK, PacketType.Play.Server.OPEN_WINDOW_MERCHANT));
        }
        Bukkit.getPluginManager().registerEvents(new GuiListeners(),this);

        keeperHeads = Arrays.asList(PlayerHeadSkins.SHOPKEEPER_1.toString(), PlayerHeadSkins.SHOPKEEPER_2.toString(), PlayerHeadSkins.SHOPKEEPER_3.toString(),
                PlayerHeadSkins.SHOPKEEPER_4.toString(), PlayerHeadSkins.SHOPKEEPER_5.toString());
        getCommand("shops").setExecutor(new Commands());
        getCommand("shop").setExecutor(new Commands());
        getCommand("playershops").setExecutor(new Commands());
        getCommand("sna").setExecutor(new Commands());
        getCommand("sna").setTabCompleter(new TabComplete());
        if(Bukkit.getPluginManager().getPlugin("PlotSquared")!=null){
            plotSquaredHook = new PlotSquaredHook();
        }



    }

    public boolean isSafeLocation(Location location) {
        if (!location.getBlock().getType().isAir() || !location.clone().add(0, 1, 0).getBlock().getType().isAir()) {
            return false;
        }
        for (int i = 1; i <= 2; i++) {
            Location checkLocation = location.clone().subtract(0, i, 0);
            if (checkLocation.getBlock().getType().isSolid()) {
                return true;
            }
        }
        return false;
    }
}
