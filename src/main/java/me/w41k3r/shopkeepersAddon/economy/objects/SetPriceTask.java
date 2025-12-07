package me.w41k3r.shopkeepersAddon.economy.objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import static me.w41k3r.shopkeepersAddon.ShopkeepersAddon.*;

public class SetPriceTask implements Listener {
    private final Player player;
    private final int slot;
    private final PriceInputCallback callback;

    public SetPriceTask(Player player, int slot, PriceInputCallback callback) {
        this.player = player;
        this.slot = slot;
        this.callback = callback;
    }

    public void startEdit() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEditPrice(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(player)) {
            return;
        }

        String message = event.getMessage().trim();

        // cancel keyword
        if (message.equalsIgnoreCase("cancel")) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> {
                sendPlayerMessage(player, config.getString("messages.priceChangeCancelled"));
                HandlerList.unregisterAll(this);
            });
            return;
        }

        double price;
        try {
            price = parsePrice(message);
        } catch (NumberFormatException e) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> {
                sendPlayerMessage(player, config.getString("messages.invalidPrice"));
            });
            return;
        }

        final double finalPrice = price;
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, () -> {
            callback.onPriceSet(finalPrice, slot);
            HandlerList.unregisterAll(this);
        });
    }

    /**
     * Parse a user input price robustly:
     * - strips non-numeric / separator characters,
     * - tries dot-normalized parse,
     * - then tries NumberFormat for common locales (US, FR).
     */
    private double parsePrice(String input) throws NumberFormatException {
        if (input == null) throw new NumberFormatException("null input");

        String orig = input.trim();

        // Validate input: only allow valid number formats (optional leading minus, digits, optional single decimal separator)
        if (!orig.matches("^-?\\d+([.,]\\d+)?$")) {
            throw new NumberFormatException("Invalid number format: " + orig);
        }

        // 1) quick try: replace comma with dot (accept "10", "10.00", "10,00")
        try {
            return Double.parseDouble(orig.replace(',', '.'));
        } catch (NumberFormatException ignored) {
        }

        // 2) try parsing with NumberFormat for common locales and ensure full consumption
        NumberFormat[] formats = new NumberFormat[]{
                NumberFormat.getInstance(Locale.US),     // 1,234.56
                NumberFormat.getInstance(Locale.FRANCE)  // 1.234,56
        };

        for (NumberFormat nf : formats) {
            ParsePosition pos = new ParsePosition(0);
            Number num = nf.parse(sanitized, pos);
            if (num != null && pos.getIndex() == sanitized.length()) {
                return num.doubleValue();
            }
        }

        // nothing matched
        throw new NumberFormatException("Invalid number: " + input);
    }
}