package me._w41k3r.shopkeepersAddon.Economy;

import java.util.function.Supplier;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;

public class Log {

    private static Logger logger;
    private static boolean debugging;

    public static void setUp(Logger logger) {
        Preconditions.checkNotNull(logger);
        Preconditions.checkState(Log.logger == null, "Already set up!");
        Log.logger = logger;
    }

    public static void setDebugging(boolean debugging) {
        Log.debugging = debugging;
    }

    private static Logger getLogger() {
        Preconditions.checkState(logger != null, "Not yet set up!");
        return logger;
    }

    public static void debug(String message) {
        if (debugging) {
            getLogger().info(message);
        }
    }

    public static void debug(Supplier<String> messageSupplier) {
        if (debugging) {
            getLogger().info(messageSupplier);
        }
    }

    private Log() {
    }
}