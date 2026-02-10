package me.w41k3r.shopkeepersAddon.economy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.milkbowl.vault.economy.EconomyResponse;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;
import static me.w41k3r.shopkeepersAddon.economy.PersistantDataManager.setPrice;

public class EconomyManager {

    private static final String ECONOMY_CONFIG_PATH = "economy.";
    private static final String PRICE_FORMAT_PATH = ECONOMY_CONFIG_PATH + "price-format.";
    private static final String PRICE_PLACEHOLDER = "%price%";
    private static final String SYMBOL_PLACEHOLDER = "%symbol%";

    private static DecimalFormat priceFormatter;
    private static String currencySymbol;
    private static boolean symbolBefore;

    public static void initializePriceFormatter() {
        try {
            // Get format pattern
            String pattern = config.getString(PRICE_FORMAT_PATH + "pattern", "#,##0.00");

            // Get currency symbol settings
            currencySymbol = config.getString(PRICE_FORMAT_PATH + "currency-symbol", "$");
            symbolBefore = config.getBoolean(PRICE_FORMAT_PATH + "symbol-before", true);

            // Create decimal format symbols
            DecimalFormatSymbols symbols = createDecimalFormatSymbols();

            // Create the formatter
            priceFormatter = new DecimalFormat(pattern, symbols);
            priceFormatter.setGroupingUsed(true);

            debugLog("Price formatter initialized with pattern: " + pattern);
            debugLog("Currency symbol: " + currencySymbol + " (position: " + (symbolBefore ? "before" : "after") + ")");

        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to initialize price formatter: " + e.getMessage());
            // Fallback to default formatter
            priceFormatter = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.US));
        }
    }

    private static DecimalFormatSymbols createDecimalFormatSymbols() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        // Check if custom separators are specified
        String customDecimalSep = config.getString(PRICE_FORMAT_PATH + "decimal-separator");
        String customGroupingSep = config.getString(PRICE_FORMAT_PATH + "grouping-separator");

        if (customDecimalSep != null && !customDecimalSep.isEmpty()) {
            symbols.setDecimalSeparator(customDecimalSep.charAt(0));
        }
        if (customGroupingSep != null && !customGroupingSep.isEmpty()) {
            symbols.setGroupingSeparator(customGroupingSep.charAt(0));
        }

        // If no custom separators, check for European format
        if (customDecimalSep == null && customGroupingSep == null) {
            boolean europeanFormat = config.getBoolean(PRICE_FORMAT_PATH + "european-format", false);
            if (europeanFormat) {
                symbols.setDecimalSeparator(',');
                symbols.setGroupingSeparator('.');
            }
        }

        return symbols;
    }

    public static ItemStack getCurrencyItem(double price, boolean isBuyItem) {
        String type = isBuyItem ? "buy-item" : "sell-item";
        String configPath = ECONOMY_CONFIG_PATH + type + ".";

        // Safe material parsing with fallback
        Material material = getMaterialSafely(config.getString(configPath + "material", "PAPER"));
        ItemStack moneyItem = new ItemStack(material);
        ItemMeta meta = moneyItem.getItemMeta();

        if (meta == null) {
            return moneyItem;
        }

        // Format the price and prepare the display text
        String formattedPrice = formatPrice(price);
        String displayText = buildDisplayText(configPath, formattedPrice);

        // Set display name
        meta.setDisplayName(displayText);

        // Set lore with formatted prices
        List<String> lore = config.getStringList(configPath + "lore");
        if (lore != null) {
            lore.replaceAll(line -> replacePlaceholders(line, formattedPrice));
        }
        meta.setLore(lore);

        // Set price in persistent data
        meta = setPrice(meta, price);

        // Add glow effect if enabled
        if (config.getBoolean(configPath + "glow", false)) {
            addGlowEffect(meta);
        }

        // Set custom model data if specified
        int customModelData = config.getInt(configPath + "custom-model-data", 0);
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }

        moneyItem.setItemMeta(meta);
        return moneyItem;
    }

    private static String buildDisplayText(String configPath, String formattedPrice) {
        String displayName = config.getString(configPath + "name");

        return replacePlaceholders(displayName, formattedPrice);
    }

    private static boolean isBuyItem(String configPath) {
        return configPath.contains("buy-item");
    }

    private static String replacePlaceholders(String text, String formattedPrice) {
        return text.replace(PRICE_PLACEHOLDER, formattedPrice)
                .replace(SYMBOL_PLACEHOLDER, currencySymbol);
    }

    private static Material getMaterialSafely(String materialName) {
        if (materialName == null || materialName.trim().isEmpty()) {
            debugLog("Material name is null or empty, using PAPER as fallback");
            return Material.PAPER;
        }

        try {
            Material material = Material.valueOf(materialName.toUpperCase().trim());
            if (!material.isItem()) {
                debugLog("Material " + materialName + " is not a valid item type, using PAPER as fallback");
                return Material.PAPER;
            }
            return material;
        } catch (IllegalArgumentException e) {
            debugLog("Invalid material name: " + materialName + ", using PAPER as fallback");
            return Material.PAPER;
        }
    }

    private static void addGlowEffect(ItemMeta meta) {
        try {
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } catch (Exception e) {
            debugLog("Failed to add glow effect to item");
        }
    }

    public static boolean hasMoney(Player player, double amount) {
        if (amount < 0) {
            debugLog("Negative amount check for player: " + player.getName());
            return false;
        }

        double balance = Money.getBalance(player);
        boolean hasEnough = balance >= amount;

        if (!hasEnough) {
            debugLog("Player " + player.getName() + " has insufficient funds. Required: " +
                    formatPrice(amount) + ", Available: " + formatPrice(balance));
        }

        return hasEnough;
    }

    // Main price formatting method
    public static String formatPrice(double price) {
        if (priceFormatter == null) {
            initializePriceFormatter(); // Fallback initialization
        }

        try {
            String formatted = priceFormatter.format(price);
            return formatWithCurrencySymbol(formatted);
        } catch (Exception e) {
            debugLog("Error formatting price " + price + ", using fallback: " + e.getMessage());
            return formatWithCurrencySymbol(String.format(Locale.US, "%.2f", price));
        }
    }

    private static String formatWithCurrencySymbol(String formattedPrice) {
        if (symbolBefore) {
            return currencySymbol + formattedPrice;
        } else {
            return formattedPrice + currencySymbol;
        }
    }

    // Format price with specific pattern (for advanced users)
    public static String formatPrice(double price, String customPattern, char decimalSep, char groupingSep) {
        try {
            DecimalFormatSymbols customSymbols = new DecimalFormatSymbols();
            customSymbols.setDecimalSeparator(decimalSep);
            customSymbols.setGroupingSeparator(groupingSep);

            DecimalFormat customFormatter = new DecimalFormat(customPattern, customSymbols);
            String formatted = customFormatter.format(price);
            return formatWithCurrencySymbol(formatted);

        } catch (Exception e) {
            debugLog("Error with custom price formatting: " + e.getMessage());
            return formatPrice(price); // Fallback to default
        }
    }

    // Method to handle money transaction with validation
    public static boolean transferMoney(Player from, Player to, double amount) {
        if (amount <= 0) {
            debugLog("Invalid transfer amount: " + formatPrice(amount));
            return false;
        }

        if (!hasMoney(from, amount)) {
            return false;
        }

        try {
            EconomyResponse withdrawResponse = Money.withdrawPlayer(from, amount);
            if (!withdrawResponse.transactionSuccess()) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to withdraw " + formatPrice(amount) +
                        " from " + from.getName() + ": " + withdrawResponse.errorMessage);
                return false;
            }
            EconomyResponse depositResponse = Money.depositPlayer(to, amount);
            if (!depositResponse.transactionSuccess()) {
                // Deposit failed - refund the sender
                Money.depositPlayer(from, amount);
                Bukkit.getLogger().log(Level.SEVERE, "Failed to deposit " + formatPrice(amount) +
                        " to " + to.getName() + ": " + depositResponse.errorMessage + " - refunded " + from.getName());
                return false;
            }
            debugLog("Transferred " + formatPrice(amount) + " from " +
                    from.getName() + " to " + to.getName());
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to transfer " + formatPrice(amount) +
                    " from " + from.getName() + " to " + to.getName(), e);
            return false;
        }
    }

    // Utility method to get current formatting info
    public static String getFormattingInfo() {
        if (priceFormatter == null) {
            return "Formatter not initialized";
        }

        DecimalFormatSymbols symbols = priceFormatter.getDecimalFormatSymbols();
        return String.format("Pattern: %s | Decimal: '%c' | Grouping: '%c' | Symbol: '%s' (%s)",
                priceFormatter.toPattern(),
                symbols.getDecimalSeparator(),
                symbols.getGroupingSeparator(),
                currencySymbol,
                symbolBefore ? "before" : "after"
        );
    }
}
