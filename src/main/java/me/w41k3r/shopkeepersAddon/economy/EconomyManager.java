package me.w41k3r.shopkeepersAddon.economy;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.w41k3r.shopkeepersAddon.ShopkeepersAddon;
import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.config;
import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.debugLog;
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

        // Use internal getBalance instead of Vault
        double balance = getBalance(player.getName());
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
            // Use internal methods instead of Vault
            takeMoney(from.getName(), amount);
            giveMoney(to.getName(), amount);
            
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
                symbolBefore ? "before" : "after");
    }

    // ========== UNIVERSAL ECONOMY INTEGRATION ==========

    private static final String COMMANDS_PATH = ECONOMY_CONFIG_PATH + "commands.";

    // Cache for balance checks to prevent spamming console commands (50ms TTL)
    private static final java.util.Map<String, Long> balanceCacheTime = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<String, Double> balanceCacheValue = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 50;

    /**
     * Get player balance using universal command-based system.
     *
     * @param playerName the player name
     * @return the player's balance, or 0.0 if unable to parse
     */
    public static double getBalance(String playerName) {
        // Check cache first
        if (balanceCacheTime.containsKey(playerName)) {
            long lastTime = balanceCacheTime.get(playerName);
            if (System.currentTimeMillis() - lastTime < CACHE_TTL_MS) {
                return balanceCacheValue.get(playerName);
            }
        }

        try {
            // Get balance check command from config
            String command = config.getString(COMMANDS_PATH + "balance-check.command", "bal {player}");
            command = command.replace("{player}", playerName);

            // Create console capture and execute command
            ConsoleCapture capture = new ConsoleCapture();
            capture.startLogCapture();
            try {
                Bukkit.dispatchCommand(capture, command);
                
                // Wait for output (max 200ms) if empty
                long start = System.currentTimeMillis();
                while (capture.getCapturedOutput().isEmpty() && System.currentTimeMillis() - start < 1000) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } finally {
                capture.stopLogCapture();
            }

            // Get captured output
            List<String> output = capture.getCapturedOutput();
            if (output.isEmpty()) {
                debugLog("No output captured from balance check command: " + command);
                return 0.0;
            }
            
            if (ShopkeepersAddon.debugMode) {
                debugLog("Captured Output for '" + command + "':");
                for (String line : output) {
                    debugLog(" > " + line);
                }
            }

            // Parse based on mode
            boolean strictMode = config.getBoolean(COMMANDS_PATH + "balance-check.strict-mode", false);
            double balance;

            if (strictMode) {
                String regexPattern = config.getString(COMMANDS_PATH + "balance-check.regex-pattern", "Balance: ([\\d,.]+)");
                balance = parseBalanceStrict(output, regexPattern);
            } else {
                balance = parseBalanceHeuristic(output);
            }

            debugLog("Parsed balance for " + playerName + ": " + formatPrice(balance));
            
            // Update cache
            balanceCacheTime.put(playerName, System.currentTimeMillis());
            balanceCacheValue.put(playerName, balance);
            
            return balance;

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to get balance for " + playerName, e);
            return 0.0;
        }
    }

    /**
     * Give money to a player using configured commands.
     *
     * @param playerName the player name
     * @param amount     the amount to give
     */
    public static void giveMoney(String playerName, double amount) {
        try {
            List<String> commands = config.getStringList(COMMANDS_PATH + "give-money");
            if (commands.isEmpty()) {
                debugLog("No give-money commands configured");
                return;
            }

            String formattedAmount = String.format(Locale.US, "%.2f", amount);

            for (String command : commands) {
                command = command.replace("{player}", playerName);
                command = command.replace("{price}", formattedAmount);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                debugLog("Executed give-money command: " + command);
            }

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to give money to " + playerName, e);
        }
    }

    /**
     * Take money from a player using configured commands.
     *
     * @param playerName the player name
     * @param amount     the amount to take
     */
    public static void takeMoney(String playerName, double amount) {
        try {
            List<String> commands = config.getStringList(COMMANDS_PATH + "take-money");
            if (commands.isEmpty()) {
                debugLog("No take-money commands configured");
                return;
            }

            String formattedAmount = String.format(Locale.US, "%.2f", amount);

            for (String command : commands) {
                command = command.replace("{player}", playerName);
                command = command.replace("{price}", formattedAmount);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                debugLog("Executed take-money command: " + command);
            }

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to take money from " + playerName, e);
        }
    }

    /**
     * Parse balance using strict regex mode.
     *
     * @param output       the captured command output
     * @param regexPattern the regex pattern with one capturing group
     * @return the parsed balance, or 0.0 if not found
     */
    private static double parseBalanceStrict(List<String> output, String regexPattern) {
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regexPattern);

            for (String line : output) {
                java.util.regex.Matcher matcher = pattern.matcher(line);
                if (matcher.find() && matcher.groupCount() >= 1) {
                    String balanceStr = matcher.group(1);
                    // Remove commas and parse
                    balanceStr = balanceStr.replace(",", "");
                    return Double.parseDouble(balanceStr);
                }
            }

            debugLog("Strict regex pattern did not match any output line");
            return 0.0;

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to parse balance in strict mode", e);
            return 0.0;
        }
    }

    /**
     * Parse balance using heuristic auto-detection mode.
     * Looks for numbers in the output, prioritizing those at the end of lines.
     *
     * @param output the captured command output
     * @return the parsed balance, or 0.0 if not found
     */
    private static double parseBalanceHeuristic(List<String> output) {
        try {
            // Iterate lines BACKWARDS (newest first)
            for (int j = output.size() - 1; j >= 0; j--) {
                String line = output.get(j);
                
                // Strip ANSI codes first
                line = line.replaceAll("\u001B\\[[;\\d]*m", "");
                
                // Strip color codes
                line = org.bukkit.ChatColor.stripColor(line);
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                // Split by whitespace first to preserve token structure
                String[] words = line.split("\\s+");

                // Iterate backwards (balance is usually at the end)
                for (int i = words.length - 1; i >= 0; i--) {
                    String word = words[i];
                    
                    // 1. Remove common currency symbols and non-numeric chars from edges
                    // We keep digits, dots, and commas. We also remove brackets/parentheses often found in logs.
                    String cleaned = word.replaceAll("[^\\d.,a-zA-Z]", "");
                    
                    // 2. If the remaining word contains ANY letters, it is likely not a balance 
                    // (e.g. "USD", "EUR", "Coins" or a username like "_w41k3r" which became "w41k3r")
                    // We want to avoid parsing "100Coins" as "100" if it risks parsing "Player1" as "1".
                    // The safest heuristic is: MUST be purely numeric (with dots/commas).
                    if (cleaned.matches(".*[a-zA-Z].*")) {
                        continue;
                    }
                    
                    // 3. Now strip everything that isn't a digit, dot, or comma to handle things like "$100.00" -> "100.00"
                    cleaned = cleaned.replaceAll("[^\\d.,]", "");

                    if (cleaned.isEmpty()) {
                        continue;
                    }

                    try {
                        // Remove commas for parsing
                        String parseable = cleaned.replace(",", "");
                        
                        // Check if it's a valid number
                        // Handle cases like "." or "," which might remain
                        if (!parseable.matches(".*\\d.*")) {
                            continue;
                        }

                        double value = Double.parseDouble(parseable);
                        // Sanity check: balance should be non-negative
                        if (value >= 0) {
                            debugLog("Found valid balance: " + value + " (Source token: '" + word + "')");
                            return value;
                        }
                    } catch (NumberFormatException ignored) {
                        // Continue to next word
                    }
                }
            }

            debugLog("Heuristic parser could not find a valid balance in output");
            return 0.0;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to parse balance in heuristic mode", e);
            return 0.0;
        }
    }

    /**
     * Check if universal economy is enabled and should be used instead of Vault.
     *
     * @return true if universal economy commands should be used
     */
    public static boolean isUniversalEconomyEnabled() {
        return config.getBoolean(ECONOMY_CONFIG_PATH + "enabled", false);
    }

    /**
     * Check if balance checking is required for buy transactions.
     *
     * @return true if balance should be checked before buying
     */
    public static boolean isBalanceCheckRequired() {
        return config.getBoolean(ECONOMY_CONFIG_PATH + "buy-settings.require-balance-check", true);
    }

    /**
     * Check if cost is disabled for buy transactions.
     *
     * @return true if items should be free
     */
    public static boolean isBuyFree() {
        return config.getBoolean(ECONOMY_CONFIG_PATH + "buy-settings.disable-cost", false);
    }

    /**
     * Check if payouts are disabled for sell transactions.
     *
     * @return true if no money should be given
     */
    public static boolean isSellFree() {
        return config.getBoolean(ECONOMY_CONFIG_PATH + "sell-settings.disable-payout", false);
    }

    /**
     * Check if owner balance checking is required for sell transactions.
     *
     * @return true if owner balance should be checked before selling
     */
    public static boolean isOwnerBalanceCheckRequired() {
        return config.getBoolean(ECONOMY_CONFIG_PATH + "sell-settings.require-owner-balance-check", true);
    }
}
