package me.w41k3r.shopkeepersaddon;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

import static me.w41k3r.shopkeepersaddon.General.UIHandler.HomePage;
import static me.w41k3r.shopkeepersaddon.General.Utils.*;
import static me.w41k3r.shopkeepersaddon.Main.sendPlayerMessage;
import static me.w41k3r.shopkeepersaddon.Main.setting;

public class Commands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] strings) {




        if (!(commandSender instanceof Player)){
            commandSender.sendMessage("You must be a player to use this command!");
            return false;
        }

        Player player = (Player) commandSender;



        if (label.equalsIgnoreCase("setshop")) {
            if (strings.length < 1) {
                return false;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    setShop(commandSender.getName(), Arrays.toString(strings), (Player) commandSender);
                }
            }.runTaskAsynchronously(Main.plugin);
            return true;
        }

        if (label.equalsIgnoreCase("visitshop")) {
            if (strings.length < 1) {
                return false;
            }
            teleportToShop((Player) commandSender, getUUIDFromName(strings[0], false).toString().trim(), false);
            return true;
        }

        if (label.equalsIgnoreCase("shops")){
            try {
                ((Player) commandSender).openInventory(HomePage);
            } catch (Exception e) {
                sendPlayerMessage(player,setting().getString("messages.no-shop"));
            }
            return true;
        }

        if (strings[0].equalsIgnoreCase("version")) {
            sendPlayerMessage(player,"§eShopkeepersAddon: §7" + Main.plugin.getDescription().getVersion());
            return true;
        }

        if (strings[0].equalsIgnoreCase("reload")) {
            Main.plugin.reloadConfig();
            sendPlayerMessage(player,"§6Shops reloaded!");
            return true;
        }

        return false;
    }

}
