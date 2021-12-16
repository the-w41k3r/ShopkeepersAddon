package dev.MrFlyn.shopkeeperNavAddon;

import dev.MrFlyn.shopkeeperNavAddon.globalshopgui.Commands;
import dev.MrFlyn.shopkeeperNavAddon.globalshopgui.GuiListeners;
import dev.MrFlyn.shopkeeperNavAddon.globalshopgui.PlayerHeadSkins;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends JavaPlugin {
    public List<String> keeperHeads =new ArrayList<>();
    public static Main plugin;
    public PluginHooks hooks = null;


    @Override
    public void onEnable(){
        saveDefaultConfig();
        plugin = this;
        Bukkit.getPluginManager().registerEvents(new GuiListeners(),this);
        keeperHeads = Arrays.asList(PlayerHeadSkins.SHOPKEEPER_1.toString(), PlayerHeadSkins.SHOPKEEPER_2.toString(), PlayerHeadSkins.SHOPKEEPER_3.toString(),
                PlayerHeadSkins.SHOPKEEPER_4.toString(), PlayerHeadSkins.SHOPKEEPER_5.toString());
        getCommand("shops").setExecutor(new Commands());
        getCommand("sna").setExecutor(new Commands());
        getCommand("sna").setTabCompleter(new TabComplete());
        if(Bukkit.getPluginManager().getPlugin("PlotSquared")!=null){
            hooks = new PluginHooks();
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
