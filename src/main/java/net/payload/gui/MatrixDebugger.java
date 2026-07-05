package net.payload.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MatrixDebugger {
    private static final Logger LOGGER = LogManager.getLogger("PayloadMatrixDebug");
    private static boolean DEBUG_ENABLED = false;

    public static void setDebugEnabled(boolean enabled) {
        DEBUG_ENABLED = enabled;
        LOGGER.info("Matrix debugging {} ", enabled ? "enabled" : "disabled");
    }

    public static void logMatrix(String hudName, String operation, String details) {
        if (!DEBUG_ENABLED) return;

        LOGGER.debug("[{}] {} - Thread: {} - Details: {}",
                hudName,
                operation,
                Thread.currentThread().getName(),
                details
        );
    }
}