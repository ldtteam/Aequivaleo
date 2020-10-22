package com.ldtteam.aequivaleo.utils;

import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.config.Configuration;
import org.apache.logging.log4j.Logger;

public class AnalysisLogHandler
{

    private static final SimpleValueCache<Boolean> CONFIG = new SimpleValueCache<>(
      Aequivaleo.getInstance().getConfiguration().getCommon().debugAnalysisLog::get
    );

    private AnalysisLogHandler() {
        throw new IllegalStateException("Tried to initialize AnalysisLogHandler. This is a utility class.");
    }

    public static void onConfigurationReloaded() {
        CONFIG.clear();
    }

    public static void debug(
      final Logger logger,
      final String string
    ) {
        if (CONFIG.get()) {
            logger.debug(string);
        }
    }
}
