package dev.MrFlyn.shopkeeperNavAddon;

import dev.MrFlyn.shopkeeperNavAddon.globalshopgui.Commands;
import dev.MrFlyn.shopkeeperNavAddon.globalshopgui.GuiListeners;
import dev.MrFlyn.shopkeeperNavAddon.globalshopgui.PlayerHeadSkins;
import org.bukkit.Bukkit;
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
        plugin = this;
        Bukkit.getPluginManager().registerEvents(new GuiListeners(),this);
        keeperHeads = Arrays.asList(PlayerHeadSkins.SHOPKEEPER_1.toString(), PlayerHeadSkins.SHOPKEEPER_2.toString(), PlayerHeadSkins.SHOPKEEPER_3.toString(),
                PlayerHeadSkins.SHOPKEEPER_4.toString(), PlayerHeadSkins.SHOPKEEPER_5.toString());
        getCommand("shops").setExecutor(new Commands());
        if(Bukkit.getPluginManager().getPlugin("PlotSquared")!=null){
            hooks = new PluginHooks();
        }

    }
}
