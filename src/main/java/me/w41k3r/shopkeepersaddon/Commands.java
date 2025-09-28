package me.w41k3r.shopkeepersAddon;

import me.w41k3r.shopkeepersAddon.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.gui.managers.PlayerShopsManager.saveShop;
import static me.w41k3r.shopkeepersAddon.gui.models.Variables.homePage;

public class Commands implements CommandExecutor, TabCompleter {

    private static final String PERMISSION_RELOAD = "shopkeepersaddon.reload";
    private static final String PERMISSION_SHOPS = "shopkeepersaddon.shops";
    private static final String PERMISSION_SETSHOP = "shopkeepersaddon.setshop";

    public static void initCommands() {
        Commands commands = new Commands();
        plugin.getCommand("shopkeepersaddon").setExecutor(commands);
        plugin.getCommand("shopkeepersaddon").setTabCompleter(commands);
        plugin.getCommand("shops").setExecutor(commands);
        plugin.getCommand("setshop").setExecutor(commands);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        try {
            return handleCommand(sender, command, alias, args);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error executing command '" + command.getName() + "': " + e.getMessage());
            e.printStackTrace();
            sendMessage(sender, "&cAn error occurred while executing this command.");
            return true;
        }
    }

    private boolean handleCommand(CommandSender sender, Command command, String alias, String[] args) {
        String commandName = command.getName().toLowerCase();

        switch (commandName) {
            case "shopkeepersaddon":
                return handleMainCommand(sender, args);
            case "shops":
                return handleShopsCommand(sender);
            case "setshop":
                return handleSetShopCommand(sender, args);
            default:
                return false;
        }
    }

    private boolean handleMainCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendInfoMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReloadCommand(sender);
            case "version":
            case "info":
                sendInfoMessage(sender);
                return true;
            default:
                sendMessage(sender, "&cUnknown subcommand. Use &7/shopkeepersaddon reload &cor &7/shopkeepersaddon version");
                return true;
        }
    }

    private boolean handleReloadCommand(CommandSender sender) {
        if (!hasPermission(sender, PERMISSION_RELOAD)) {
            sendMessage(sender, "&cYou don't have permission to use this command!");
            return true;
        }

        CompletableFuture.runAsync(() -> {
            try {
                sendMessage(sender, "&7Reloading ShopkeepersAddon configuration...");

                long startTime = System.currentTimeMillis();

                // Reload config and call the main class's loadConfiguration method
                plugin.reloadConfig();
                config = plugin.getConfig();

                // Reinitialize price formatter
                if (config.getBoolean("economy.enabled", false)) {
                    EconomyManager.initializePriceFormatter();
                }

                // Use the existing loadConfiguration method from main class
                loadConfiguration();

                long reloadTime = System.currentTimeMillis() - startTime;

                sendMessage(sender, "&aConfiguration reloaded successfully! (&7" + reloadTime + "ms&a)");
                debugLog("Configuration reloaded by " + sender.getName());

            } catch (Exception e) {
                Bukkit.getLogger().severe("Error during reload: " + e.getMessage());
                e.printStackTrace();
                sendMessage(sender, "&cError reloading configuration. Check console for details.");
            }
        });

        return true;
    }

    private boolean handleShopsCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "&cThis command can only be used by players!");
            return true;
        }

        if (!hasPermission(sender, PERMISSION_SHOPS)) {
            sendMessage(sender, "&cYou don't have permission to use this command!");
            return true;
        }

        if (homePage == null) {
            sendMessage(player, "&cShop GUI is not ready yet. Please try again in a moment.");
            return true;
        }

        player.openInventory(homePage);
        return true;
    }

    private boolean handleSetShopCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "&cThis command can only be used by players!");
            return true;
        }

        if (!hasPermission(sender, PERMISSION_SETSHOP)) {
            sendMessage(sender, "&cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sendMessage(player, "&cUsage: /setshop <shop name>");
            sendMessage(player, "&7You can use \\n for new lines in the shop name.");
            return true;
        }

        // Build shop name from arguments
        String shopName = String.join(" ", args).trim();
        if (shopName.isEmpty()) {
            sendMessage(player, "&cShop name cannot be empty!");
            return true;
        }

        // Process shop name with newline support
        List<String> shopNameLines = Arrays.asList(shopName.split("\\\\n"));

        CompletableFuture.runAsync(() -> {
            try {
                saveShop(shopNameLines, player);
                debugLog("Shop saved for player: " + player.getName());
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error saving shop for player " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
                sendMessage(player, "&cAn error occurred while saving your shop. Please try again.");
            }
        });

        return true;
    }

    private void sendInfoMessage(CommandSender sender) {
        sendMessage(sender, "&6&lShopkeepersAddon &7v" + plugin.getDescription().getVersion());
        sendMessage(sender, "&7Author: &f" + String.join(", ", plugin.getDescription().getAuthors()));
        sendMessage(sender, "&7Description: &f" + plugin.getDescription().getDescription());

        if (sender.hasPermission(PERMISSION_RELOAD)) {
            sendMessage(sender, "&7Commands: &f/shopkeepersaddon reload &8- &7Reload configuration");
        }
        sendMessage(sender, "&7Commands: &f/shops &8- &7Open shops GUI");
        sendMessage(sender, "&7Commands: &f/setshop <name> &8- &7Set your shop name");
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission) || sender.isOp();
    }

    private void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player) {
            sendPlayerMessage((Player) sender, message);
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("shopkeepersaddon")) {
            if (args.length == 1) {
                List<String> options = Arrays.asList("reload", "version", "info");
                return StringUtil.copyPartialMatches(args[0], options, new ArrayList<>());
            }
        }

        return completions;
    }
}