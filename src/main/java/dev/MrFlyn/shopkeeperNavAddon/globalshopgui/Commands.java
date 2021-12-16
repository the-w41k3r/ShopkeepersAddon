package dev.MrFlyn.shopkeeperNavAddon.globalshopgui;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
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
                sender.sendMessage("§aYou do not have the required permission execute the command.");
                return true;
            }
            if(!(sender instanceof Player))
            {
                sender.sendMessage("§aConsole cannot execute this command.");
                return true;
            }
            Player p = (Player) sender;

            ShopGUI.openShopGUI(p, MenuType.MAIN_MENU);


            return true;
        }
        return true;
    }
}
