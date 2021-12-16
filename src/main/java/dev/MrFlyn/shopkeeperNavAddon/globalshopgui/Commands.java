package dev.MrFlyn.shopkeeperNavAddon.globalshopgui;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import dev.MrFlyn.shopkeeperNavAddon.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
        else if(cmd.getName().equalsIgnoreCase("sna")){
            if(args.length<1){
                sender.sendMessage("§cInvalid arguments. Please try /sna help.");
                return true;
            }
            switch (args[0]){
                case "help":
                    if(sender.hasPermission("SNA.command.help")) {
                        sender.sendMessage("§aShopkeepersNavigationAddon\nAvailableCommands:\n/shops\n/sna reload\n/sna help");
                        return true;
                    }
                    else {
                        sender.sendMessage("§cYou do not have the required permission to execute this command.");
                    }
                    break;
                case "reload":
                    if (sender.hasPermission("SNA.admin")) {
                        Main.plugin.reloadConfig();
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

            }
        }
        return true;
    }
}
