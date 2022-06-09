package me._w41k3r.shopkeepersAddon;


import me._w41k3r.shopkeepersAddon.Economy.Log;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Preconditions;

public class Config {

    private final Plugin plugin;

    public boolean debug;

    Config(Plugin plugin) {
        Preconditions.checkNotNull(plugin);
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        Configuration config = plugin.getConfig();

        debug = config.getBoolean("debug");
        Log.setDebugging(debug);
    }
}