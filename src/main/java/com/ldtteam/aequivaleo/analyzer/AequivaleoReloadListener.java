package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.Lists;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.event.OnAnalysisCompleted;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.bootstrap.WorldBootstrapper;
import com.ldtteam.aequivaleo.results.ResultsInformationCache;
import net.minecraft.resources.IResourceManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class AequivaleoReloadListener implements ISelectiveResourceReloadListener
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
    public void onResourceManagerReload(final IResourceManager resourceManager, final Predicate<IResourceType> resourcePredicate)
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

        LOGGER.info("Reloading world information");
        worlds.forEach(WorldBootstrapper::onWorldReload);

        LOGGER.info("Analyzing information");
        final ClassLoader classLoader = Aequivaleo.class.getClassLoader();
        final AtomicInteger genericThreadCounter = new AtomicInteger();
        final Executor aequivaleoReloadExecutor = Executors.newFixedThreadPool(worlds.size() + 4, runnable -> {
            final Thread thread = new Thread(runnable);
            thread.setContextClassLoader(classLoader);
            thread.setName("Aequivaleo analysis runner: " + genericThreadCounter.incrementAndGet());
            return thread;
        });

        CompletableFuture.allOf(worlds.stream().map(world -> CompletableFuture.runAsync(
          new AequivaleoWorldAnalysisRunner(world),
          aequivaleoReloadExecutor
        )).toArray(CompletableFuture[]::new))
          .thenRunAsync(ResultsInformationCache::updateAllPlayers)
          .thenRunAsync(() -> MinecraftForge.EVENT_BUS.post(new OnAnalysisCompleted()));
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
            LOGGER.info("Starting equivalency data reload for world: " + serverWorld.func_234923_W_().func_240901_a_().toString());

            JGraphTBasedCompoundAnalyzer analyzer = new JGraphTBasedCompoundAnalyzer(serverWorld);
            ResultsInformationCache.getInstance(serverWorld.func_234923_W_()).set(analyzer.calculateAndGet());
        }

        public ServerWorld getServerWorld()
        {
            return serverWorld;
        }
    }
}
