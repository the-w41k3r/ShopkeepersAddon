package me._w41k3r.shopkeepersAddon.Economy;

import me._w41k3r.shopkeepersAddon.Main;
import org.bukkit.configuration.file.YamlConfiguration;


import java.io.File;

public class ConfigHandler {
    public static void load(){
        Main.plugin.reloadConfig();
        Main.plugin.messages = YamlConfiguration.loadConfiguration(new File(Main.plugin.getDataFolder()+"/messages.yml"));
    }
}
