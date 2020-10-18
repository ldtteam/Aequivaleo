package com.ldtteam.aequivaleo.recipe.equivalency;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.util.Constants;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.LevelRangeFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class RecipeCalculatorLogHandler
{

    private static final Logger LOGGER = LogManager.getLogger();

    private RecipeCalculatorLogHandler() {
        throw new IllegalStateException("Tried to instantiate a utility class.");
    }

    public static void setupLogging() {
        getLoggerNames().forEach(
          logger -> {
              final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
              final Configuration config = ctx.getConfiguration();
              final LoggerConfig loggerConfig = getLoggerConfiguration(config, logger);

              final RollingRandomAccessFileAppender appender = RollingRandomAccessFileAppender.newBuilder()
                                                                 .setName("ErrorLog")
                                                                 .withFileName("logs/aequivaleo/ingredient/error.log")
                                                                 .withFilePattern("logs/aequivaleo/ingredient/%d{yyyy-MM-dd}-%i.error.gz")
                                                                 .setLayout(PatternLayout.newBuilder()
                                                                              .withPattern("[%d{ddMMMyyyy HH:mm:ss.SSS}] [%t/%level] [%logger/%markerSimpleName]: %minecraftFormatting{%msg}{strip}%n%xEx")
                                                                              .build()
                                                                 )
                                                                 .withPolicy(CompositeTriggeringPolicy.createPolicy(
                                                                   TimeBasedTriggeringPolicy.newBuilder()
                                                                     .withInterval(1)
                                                                     .withMaxRandomDelay(0)
                                                                     .withModulate(false)
                                                                     .build(),
                                                                   OnStartupTriggeringPolicy.createPolicy(1)
                                                                 ))
                                                                 .build();

              appender.start();

              loggerConfig.setParent(null);
              loggerConfig.getAppenders().keySet().forEach(loggerConfig::removeAppender);
              loggerConfig.addAppender(
                appender,
                Level.ERROR,
                null
              );
          }
        );

        LOGGER.warn("Updated the RecipeCalculation ingredient logs.");
    }

    private static Set<String> getLoggerNames() {
        return Sets.newHashSet(
          "com.ldtteam.aequivaleo.recipe.equivalency.RecipeCalculator.IngredientHandler.IngredientLoggingHandler",
          "com.ldtteam.aequivaleo.recipe.equivalency.RecipeCalculator.IngredientHandler.FullLoggingHandler"
        );
    }

    private static LoggerConfig getLoggerConfiguration(@NotNull final Configuration configuration, @NotNull final String loggerName)
    {
        final LoggerConfig lc = configuration.getLoggerConfig(loggerName);
        if (lc.getName().equals(loggerName))
        {
            return lc;
        }
        else
        {
            final LoggerConfig nlc = new LoggerConfig(loggerName, lc.getLevel(), lc.isAdditive());
            nlc.setParent(lc);
            configuration.addLogger(loggerName, nlc);
            configuration.getLoggerContext().updateLoggers();

            return nlc;
        }
    }
}
