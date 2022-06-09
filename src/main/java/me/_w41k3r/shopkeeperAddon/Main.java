package me._w41k3r.shopkeeperAddon;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import me._w41k3r.shopkeeperAddon.Economy.ConfigHandler;
import me._w41k3r.shopkeeperAddon.Economy.EcoListeners;
import me._w41k3r.shopkeeperAddon.Economy.PacketInterceptor;
import me._w41k3r.shopkeeperAddon.GlobalShopGui.Commands;
import me._w41k3r.shopkeeperAddon.GlobalShopGui.GuiListeners;
import me._w41k3r.shopkeeperAddon.GlobalShopGui.PlayerHeadSkins;
import me._w41k3r.shopkeeperAddon.PluginHooks.PlotSquaredHook;
import me._w41k3r.shopkeeperAddon.PluginHooks.VaultHook;
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
        Block feet = location.getBlock();
        if (!feet.getType().isTransparent() && !feet.getLocation().add(0, 1, 0).getBlock().getType().isTransparent()) {
            return false; // not transparent (will suffocate)
        }
        Block head = feet.getRelative(BlockFace.UP);
        if (!head.getType().isTransparent()) {
            return false; // not transparent (will suffocate)
        }
        Block ground = feet.getRelative(BlockFace.DOWN);
        if (!ground.getType().isSolid()) {
            return false; // not solid
        }
        if (feet.getType() != Material.AIR) {
            return false; // not solid
        }
        return true;
    }
}
