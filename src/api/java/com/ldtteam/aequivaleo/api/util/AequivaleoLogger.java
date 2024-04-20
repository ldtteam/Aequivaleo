package com.ldtteam.aequivaleo.api.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for handling logging.
 * Creates one central logger for the mod.
 */
public class AequivaleoLogger
{

    private static final Logger log;

    static {
        log = LogManager.getLogger(Constants.MOD_ID);
    }

    private AequivaleoLogger()
    {
        throw new IllegalStateException("Tried to create utility class!");
    }

    /**
     * Starts a big warning.
     *
     * @param warningName The name of the warning.
     */
    public static void startBigWarning(String warningName)
    {
        log.warn("*******************   " + warningName + "   *********************");
    }

    /**
     * Ends a big warning.
     *
     * @param warningName The name of the warning.
     */
    public static void endBigWarning(String warningName)
    {
        log.warn("*******************   " + warningName + "   *********************");
    }

    /**
     * Appends a big warning message to a previously started big warning.
     *
     * @param format The format of the message.
     * @param data The data to log.
     */
    public static void bigWarningMessage(String format, Object... data)
    {
        log.warn("* "+format, data);
    }

    /**
     * Logs a big warning message.
     * <p>
     *     This creates a simple big warning message, without name, and only one message.
     * </p>
     * @param format The format of the message.
     * @param data The data to log.
     */
    public static void bigWarningSimple(String format, Object... data)
    {
        log.warn("****************************************");
        bigWarningMessage(format, data);
        log.warn("****************************************");
    }

    /**
     * Logs a big warning message with a stack trace.
     * <p>
     *     This creates a simple big warning message, without name, and only one message.
     *     However it does output the stacktrace.
     * </p>
     * @param format The format of the message.
     * @param data The data to log.
     */
    public static void bigWarningWithStackTrace(String format, Object... data)
    {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        log.warn("****************************************");
        log.warn("* "+format, data);
        for (int i = 2; i < 8 && i < trace.length; i++)
        {
            log.warn("*  at {}{}", trace[i].toString(), i == 7 ? "..." : "");
        }
        log.warn("****************************************");
    }

    /**
     * Logs a message with a specific level.
     *
     * @param level The level to log at.
     * @param format The format of the message.
     * @param data The data to log.
     */
    public static void log(Level level, String format, Object... data)
    {
        log.log(level, format, data);
    }

    /**
     * Logs a severe message.
     *
     * @param format The format of the message.
     * @param data The data to log.
     */
    public static void severe(String format, Object... data)
    {
        log(Level.ERROR, format, data);
    }

    /**
     * Logs a warning message.
     *
     * @param format The format of the message.
     * @param data The data to log.
     */
    public static void warning(String format, Object... data)
    {
        log(Level.WARN, format, data);
    }

    /**
     * Logs an info message.
     *
     * @param format The format of the message.
     * @param data The data to log.
     */
    public static void info(String format, Object... data)
    {
        log(Level.INFO, format, data);
    }

    /**
     * Logs a debug message.
     *
     * @param format The format of the message.
     * @param data The data to log.
     */
    public static void fine(String format, Object... data)
    {
        log(Level.DEBUG, format, data);
    }

    /**
     * Logs a trace message.
     *
     * @param format The format of the message.
     * @param data The data to log.
     */
    public static void finer(String format, Object... data)
    {
        log(Level.TRACE, format, data);
    }

    /**
     * Gives access to the underlying logger.
     *
     * @return The logger.
     */
    public static Logger getLogger()
    {
        return log;
    }
}
