package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.Lists;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.bootstrap.WorldBootstrapper;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.results.ResultsInformationCache;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class AequivaleoReloadListener extends ReloadListener<Object>
{

    private static final Logger LOGGER = LogManager.getLogger(AequivaleoReloadListener.class);

    @SubscribeEvent
    public static void onAddReloadListener(final AddReloadListenerEvent reloadListenerEvent)
    {
        LOGGER.info("Registering reload listener for graph rebuilding.");
        reloadListenerEvent.addListener(new AequivaleoReloadListener());
    }

    @SubscribeEvent
    public static void onServerStarted(final FMLServerStartedEvent serverStartedEvent)
    {
        LOGGER.info("Building initial equivalency graph.");
        reloadResources();
    }

    @Override
    protected Object prepare(final IResourceManager resourceManagerIn, final IProfiler profilerIn)
    {
        return new Object();
    }

    @Override
    protected void apply(final Object objectIn, final IResourceManager resourceManagerIn, final IProfiler profilerIn)
    {
        LOGGER.info("Reloading resources has been triggered, recalculating graph.");
        reloadResources();
    }

    private static void reloadResources()
    {
        if (ServerLifecycleHooks.getCurrentServer() == null)
        {
            return;
        }

        final List<ServerWorld> worlds = Lists.newArrayList(ServerLifecycleHooks.getCurrentServer().getWorlds());

        LOGGER.info("Analyzing information");
        final ClassLoader classLoader = Aequivaleo.class.getClassLoader();
        final AtomicInteger genericThreadCounter = new AtomicInteger();
        final int maxThreadCount = Math.max(4, Runtime.getRuntime().availableProcessors() - 2);
        final ExecutorService aequivaleoReloadExecutor = Executors.newFixedThreadPool(maxThreadCount, runnable -> {
            final Thread thread = new Thread(runnable);
            thread.setContextClassLoader(classLoader);
            thread.setName(String.format("Aequivaleo analysis runner: %s", genericThreadCounter.incrementAndGet()));
            return thread;
        });

        CompletableFuture.allOf(worlds.stream().map(world -> CompletableFuture.runAsync(
          new AequivaleoWorldAnalysisRunner(world),
          aequivaleoReloadExecutor
        )).toArray(CompletableFuture[]::new))
          .thenRunAsync(ResultsInformationCache::updateAllPlayers)
          .thenRunAsync(() -> {
              worlds.stream().forEach(world -> {
                  PluginManger.getInstance().getPlugins().parallelStream().forEach(plugin -> plugin.onReloadFinishedFor(world));
                }
              );
          })
          .thenRunAsync(aequivaleoReloadExecutor::shutdown);
    }

    private static class AequivaleoWorldAnalysisRunner implements Runnable
    {

        private final ServerWorld serverWorld;

        private AequivaleoWorldAnalysisRunner(final ServerWorld serverWorld) {this.serverWorld = serverWorld;}

        @Override
        public void run()
        {
            reloadEquivalencyData();
        }

        private void reloadEquivalencyData()
        {
            LOGGER.info("Starting aequivaleo data reload for world: " + getServerWorld().func_234923_W_().func_240901_a_().toString());
            try {
                WorldBootstrapper.onWorldReload(getServerWorld());
                JGraphTBasedCompoundAnalyzer analyzer = new JGraphTBasedCompoundAnalyzer(getServerWorld());
                ResultsInformationCache.getInstance(getServerWorld().func_234923_W_()).set(analyzer.calculateAndGet());
            } catch (Throwable t) {
                LOGGER.fatal(String.format("Failed to analyze: %s", getServerWorld().func_234923_W_().func_240901_a_()), t);
            }
            LOGGER.info("Finished aequivaleo data reload for world: " + getServerWorld().func_234923_W_().func_240901_a_().toString());
        }

        public ServerWorld getServerWorld()
        {
            return serverWorld;
        }
    }
}
