package me.w41k3r.shopkeepersAddon.economy.events;

import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import org.bukkit.entity.Player;

class BulkTradeInfo {
    final Shopkeeper shopkeeper;
    final TradingRecipe recipe;
    final Player player;
    int tradeCount;
    long startTime;

    BulkTradeInfo(Shopkeeper shopkeeper, TradingRecipe recipe, Player player) {
        this.shopkeeper = shopkeeper;
        this.recipe = recipe;
        this.player = player;
        this.tradeCount = 0;
        this.startTime = System.currentTimeMillis();
    }
}
