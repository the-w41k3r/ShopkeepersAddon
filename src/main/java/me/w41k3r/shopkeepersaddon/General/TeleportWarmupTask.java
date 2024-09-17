package me.w41k3r.shopkeepersaddon.General;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static me.w41k3r.shopkeepersaddon.Main.sendPlayerMessage;
import static me.w41k3r.shopkeepersaddon.Main.setting;

public class TeleportWarmupTask implements Listener {
    private final Player player;
    private final Location initialLocation;
    private final Location teleportLocation;
    private final String cancelMessage;
    private final boolean allowMovement;
    private final String successMessage;
    private final int warmupTime; // in seconds
    private final Plugin plugin;
    private final boolean isAdminShop; // Flag to distinguish admin and player shops

    private BukkitTask warmupTask;
    private int countdown; // Track remaining time

    public TeleportWarmupTask(Player player, Location teleportLocation, int warmupTime, boolean allowMovement, String cancelMessage, String successMessage, Plugin plugin, boolean isAdminShop) {
        this.player = player;
        this.initialLocation = player.getLocation(); // Store initial location
        this.teleportLocation = teleportLocation;
        this.cancelMessage = cancelMessage;
        this.warmupTime = warmupTime;
        this.allowMovement = allowMovement;
        this.successMessage = successMessage;
        this.plugin = plugin;
        this.countdown = warmupTime; // Set countdown to warmup time
        this.isAdminShop = isAdminShop; // Set the flag for admin shop
    }

    // Start the warmup process with countdown and movement check
    public void startWarmup() {
        // Register PlayerMoveEvent listener if movement isn't allowed
        if (!allowMovement) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        // Start the countdown task
        warmupTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdown <= 0) {
                    // Teleport player when countdown reaches zero
                    teleportPlayer();
                    cancel(); // Cancel the task
                    return;
                }

                // Update title and chat message with countdown
                player.sendTitle(setting().getString("messages.teleport-title"), setting().get("messages.teleport-subtitle").toString().replace("%time%", String.valueOf(countdown)), 0, 20, 0);
                countdown--; // Decrement countdown
            }
        }.runTaskTimer(plugin, 0, 20); // Repeat every 20 ticks (1 second)
    }

    // Teleport the player
    // Teleport the player
    private void teleportPlayer() {
        PlayerMoveEvent.getHandlerList().unregister(this);

        Location finalLocation;
        if (isAdminShop) {
            finalLocation = getSafeLocationNearby(teleportLocation);
            player.teleport(finalLocation);
            setPlayerFacingBlock(player, teleportLocation);
        } else {
            finalLocation = teleportLocation;
            player.teleport(finalLocation);
        }

        sendPlayerMessage(player,successMessage);
        player.resetTitle();
    }

    private void setPlayerFacingBlock(Player player, Location blockLocation) {
        blockLocation = blockLocation.clone().add(0.5, 0.5, 0.5);

        Location playerLocation = player.getLocation().clone();

        double deltaX = blockLocation.getX() - playerLocation.getX();
        double deltaY = blockLocation.getY() - playerLocation.getY();
        double deltaZ = blockLocation.getZ() - playerLocation.getZ();

        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
        if (yaw < 0) yaw += 360;

        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, horizontalDistance));

        player.teleport(blockLocation);

        playerLocation.setYaw(yaw);
        playerLocation.setPitch(pitch);

        player.teleport(playerLocation);
    }


    private Location getSafeLocationNearby(Location location) {
        World world = location.getWorld();
        if (world == null) return location;

        int[] cardinalDistances = {3, 2, 1};
        double[] diagonalOffsets = {2.0, -2.0};
        for (int distance : cardinalDistances) {
            // North
            Location northLocation = location.clone().add(0, 0, -distance).add(0.5, 0, 0.5); // Center of block
            if (isSafeLocation(northLocation)) {
                return northLocation;
            }

            // South
            Location southLocation = location.clone().add(0, 0, distance).add(0.5, 0, 0.5); // Center of block
            if (isSafeLocation(southLocation)) {
                return southLocation;
            }

            // East
            Location eastLocation = location.clone().add(distance, 0, 0).add(0.5, 0, 0.5); // Center of block
            if (isSafeLocation(eastLocation)) {
                return eastLocation;
            }

            // West
            Location westLocation = location.clone().add(-distance, 0, 0).add(0.5, 0, 0.5); // Center of block
            if (isSafeLocation(westLocation)) {
                return westLocation;
            }
        }

        // Check diagonal directions if no safe block found in cardinal directions
        for (double offsetX : diagonalOffsets) {
            for (double offsetZ : diagonalOffsets) {
                // Avoid checking the origin
                if (offsetX != 0 && offsetZ != 0) {
                    Location diagonalLocation = location.clone().add(offsetX, 0, offsetZ).add(0.5, 0, 0.5); // Center of block
                    if (isSafeLocation(diagonalLocation)) {
                        return diagonalLocation;
                    }
                }
            }
        }

        return location; // Fallback if no safe block found
    }


    // Helper method to determine if a location is safe
    private boolean isSafeLocation(Location location) {
        Block block = location.getBlock();
        return block.getType() == Material.AIR && block.getRelative(0, -1, 0).getType().isSolid();
    }




    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer().equals(player) && hasPlayerMoved(event.getFrom(), event.getTo())) {
            cancelTeleport();
        }
    }

    // Check if the player moved from their initial location
    private boolean hasPlayerMoved(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() ||
                from.getBlockY() != to.getBlockY() ||
                from.getBlockZ() != to.getBlockZ();
    }

    // Cancel the teleport
    private void cancelTeleport() {
        // Unregister the PlayerMoveEvent listener
        PlayerMoveEvent.getHandlerList().unregister(this);

        // Cancel the warmup task
        if (warmupTask != null) {
            warmupTask.cancel();
        }

        // Send cancel message to the player
        sendPlayerMessage(player,cancelMessage);
        player.resetTitle();
    }
}
