package me._w41k3r.shopkeepersAddon.GlobalShopGui;

import me._w41k3r.shopkeepersAddon.Economy.ConfigHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
        if(cmd.getName().equalsIgnoreCase("shops"))
        {
            if(!sender.hasPermission("SNA.command.shops")){
                sender.sendMessage("§cYou do not have the required permission execute the command.");
                return true;
            }
            if(!(sender instanceof Player))
            {
                sender.sendMessage("§cConsole cannot execute this command.");
                return true;
            }
            Player p = (Player) sender;
            ShopGUI.openShopGUI(p, MenuType.MAIN_MENU);
            return true;
        }
        else if(cmd.getName().equalsIgnoreCase("playershops")){
            if(!sender.hasPermission("SNA.command.playershops")){
                sender.sendMessage("§cYou do not have the required permission execute the command.");
                return true;
            }
            if(!(sender instanceof Player))
            {
                sender.sendMessage("§cConsole cannot execute this command.");
                return true;
            }
            Player p = (Player) sender;
            ShopGUI.openShopGUI(p, MenuType.PLAYER_SHOPS);
            return true;
        }
        else if(cmd.getName().equalsIgnoreCase("shop")){
            if(!sender.hasPermission("SNA.command.shop")){
                sender.sendMessage("§cYou do not have the required permission execute the command.");
                return true;
            }
            if(!(sender instanceof Player))
            {
                sender.sendMessage("§cConsole cannot execute this command.");
                return true;
            }
            Player p = (Player) sender;
            ShopGUI.openShopGUI(p, MenuType.REMOTE_ADMIN_SHOP);
            return true;
        }
        else if(cmd.getName().equalsIgnoreCase("sna")){
            if(args.length<1){
                sender.sendMessage("§cInvalid arguments. Please try /sna help.");
                return true;
            }
            switch (args[0]){
                case "help":
                    if(sender.hasPermission("SNA.command.help")) {
                        sender.sendMessage("§aShopkeepersNavigationAddon\nAvailableCommands:\n/shops\n/playershops\n/shop\n/sna reload\n/sna help" +
                                "\n/sna shops\n/sna shop\n/sna playershops");
                        return true;
                    }
                    else {
                        sender.sendMessage("§cYou do not have the required permission to execute this command.");
                    }
                    break;
                case "reload":
                    if (sender.hasPermission("SNA.admin")) {
                        ConfigHandler.load();
                        sender.sendMessage("§aShopkeepersNavigationAddon reloaded successfully.");
                    }
                    else {
                        sender.sendMessage("§cYou do not have the required permission to execute this command.");
                    }
                    break;
                case "shops":
                    if (!sender.hasPermission("SNA.command.shops")) {
                        sender.sendMessage("§cYou do not have the required permission execute the command.");
                        return true;
                    }
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§cConsole cannot execute this command.");
                        return true;
                    }
                    Player p = (Player) sender;
                    ShopGUI.openShopGUI(p, MenuType.MAIN_MENU);
                    break;
                case "shop":
                    if(!sender.hasPermission("SNA.command.shop")){
                        sender.sendMessage("§cYou do not have the required permission execute the command.");
                        return true;
                    }
                    if(!(sender instanceof Player))
                    {
                        sender.sendMessage("§cConsole cannot execute this command.");
                        return true;
                    }
                    Player p1 = (Player) sender;
                    ShopGUI.openShopGUI(p1, MenuType.REMOTE_ADMIN_SHOP);
                    break;
                case "playershops":
                    if(!sender.hasPermission("SNA.command.playershops")){
                        sender.sendMessage("§cYou do not have the required permission execute the command.");
                        return true;
                    }
                    if(!(sender instanceof Player))
                    {
                        sender.sendMessage("§cConsole cannot execute this command.");
                        return true;
                    }
                    Player p2 = (Player) sender;
                    ShopGUI.openShopGUI(p2, MenuType.PLAYER_SHOPS);
                    break;
            }
        }
        return true;
    }
}
