package me.w41k3r.shopkeepersAddon;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.w41k3r.shopkeepersAddon.gui.listeners.UpdateListeners.updateShops;
import static me.w41k3r.shopkeepersAddon.gui.managers.PlayerShopsManager.saveShop;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.homePage;
import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.plugin;

public class Commands implements CommandExecutor {

    /*
     * This class defines all the commands for the ShopkeepersAddon plugin addon.
     * * The commands are registered in the plugin.yml file.
     * */

    public static void initCommands() {
        plugin.getCommand("shops").setExecutor(new Commands());
        plugin.getCommand("setshop").setExecutor(new Commands());
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to use this command!");
            return false;
        }
        switch (command.getName().toLowerCase()){
            case "shops":
                Player p = (Player) commandSender;
                p.openInventory(homePage);
                return true;
            case "setshop":
                if (args.length < 1) {
                    return false;
                }
                new Thread(() -> {
                        // Convert args to a single string
                        StringBuilder argsBuilder = new StringBuilder();
                        for (String arg : args) {
                            argsBuilder.append(arg).append(" ");
                        }
                        String shopName = argsBuilder.toString().trim();
                        java.util.List<String> shopNameList = java.util.Arrays.asList(shopName.split("\\\\n"));
                        saveShop(shopNameList, (Player) commandSender);
                    }).start();

                return true;

            default:
                return false;
        }

    }

}
