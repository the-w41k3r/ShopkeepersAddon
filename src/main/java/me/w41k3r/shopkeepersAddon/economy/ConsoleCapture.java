package me.w41k3r.shopkeepersAddon.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

/**
 * A custom ConsoleCommandSender implementation that captures all messages sent to it.
 * This implementation uses Log4j Appender to intercept ALL logs, including those bypassing standard Bukkit logger.
 */
public class ConsoleCapture extends AbstractAppender implements ConsoleCommandSender {

    private final List<String> capturedOutput = new CopyOnWriteArrayList<>();
    private final Server server;

    public ConsoleCapture() {
        super("ShopkeepersAddon-Capture", null, PatternLayout.createDefaultLayout(), true, Property.EMPTY_ARRAY);
        this.server = Bukkit.getServer();
    }

    private final ThreadLocal<Boolean> isAppending = ThreadLocal.withInitial(() -> false);

    @Override
    public void append(LogEvent event) {
        if (isAppending.get()) {
            return;
        }
        
        try {
            isAppending.set(true);
            String msg = event.getMessage().getFormattedMessage();
            if (msg != null) {
                // Ignore our own capture tags to be double safe
                if (msg.contains("[ShopkeepersAddon] [Capture]")) {
                    return;
                }

                capturedOutput.add(msg);
            }
        } finally {
            isAppending.set(false);
        }
    }

    public void startLogCapture() {
        Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.addAppender(this);
        this.start();
    }

    public void stopLogCapture() {
        Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.removeAppender(this);
        this.stop();
    }

    /**
     * Get all captured output messages.
     *
     * @return List of captured messages
     */
    public List<String> getCapturedOutput() {
        return new ArrayList<>(capturedOutput);
    }

    /**
     * Clear all captured messages.
     */
    public void clearOutput() {
        capturedOutput.clear();
    }

    // --- ConsoleCommandSender Implementation Stubs ---

    @Override
    public void sendMessage(String message) {
        // No-op: We capture via Appender, but if something sends directly to us, we capture it too
        capturedOutput.add(message);
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            capturedOutput.add(message);
        }
    }

    @Override
    public void sendMessage(UUID sender, String message) {
        capturedOutput.add(message);
    }

    @Override
    public void sendMessage(UUID sender, String... messages) {
        for (String message : messages) {
            capturedOutput.add(message);
        }
    }

    @Override
    public void sendRawMessage(String message) {
        capturedOutput.add(message);
    }

    @Override
    public void sendRawMessage(UUID sender, String message) {
        capturedOutput.add(message);
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public String getName() {
        return "ConsoleCapture";
    }

    @Override
    public Spigot spigot() {
        return new Spigot();
    }

    @Override
    public boolean isPermissionSet(String name) {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return true;
    }

    @Override
    public boolean hasPermission(String name) {
        return true;
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
    }

    @Override
    public void recalculatePermissions() {
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return Set.of();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(String input) {
    }

    @Override
    public boolean beginConversation(Conversation conversation) {
        return false;
    }

    @Override
    public void abandonConversation(Conversation conversation) {
    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
    }
}
