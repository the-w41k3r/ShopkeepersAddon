package me._w41k3r.shopkeepersAddon.PluginHooks;

import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import me._w41k3r.shopkeepersAddon.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlotSquaredHook {
    public PlotSquaredHook(){

    }
   public void teleportPlayerPlotSquared(Player p, Location loc){
        try {
            PlotAPI api = new PlotAPI();
            PlotPlayer pP = api.wrapPlayer(p.getUniqueId());
            BlockVector3 Bv3 = BukkitAdapter.asBlockVector(loc);
            for (Plot plot : api.getAllPlots()) {
                for (CuboidRegion r : plot.getRegions()) {
                    if (r.contains(Bv3)) {
                        plot.teleportPlayer(pP, (cn) -> {
                        });
                        return;
                    }
                }
            }
            p.closeInventory();
            if(Main.plugin.isSafeLocation(loc)) {
                p.sendMessage("§aUnable to find plot. Teleporting to Entity instead.");
                p.teleport(loc);
            }
            else {
                p.sendMessage("§cUnsafe location detected. Cancelling teleport...");
            }
        }
        catch (Exception e){
            p.closeInventory();
            if(Main.plugin.isSafeLocation(loc)) {
                p.sendMessage("§aUnable to find plot. Teleporting to Entity instead.");
                p.teleport(loc);
            }
            else {
                p.sendMessage("§cUnsafe location detected. Cancelling teleport...");
            }

        }

   }
}
