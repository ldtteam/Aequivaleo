package com.ldtteam.aequivaleo.plugin;

import com.google.common.collect.ImmutableSet;
import com.ldtteam.aequivaleo.api.plugin.AequivaleoPlugin;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPluginManager;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.api.util.StreamUtils;
import com.ldtteam.aequivaleo.utils.ClassUtils;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class PluginManger implements IAequivaleoPluginManager
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Type AEQUIVALEO_PLUGIN_TYPE = Type.getType(AequivaleoPlugin.class);

    private static final PluginManger INSTANCE = new PluginManger();

    public static PluginManger getInstance()
    {
        return INSTANCE;
    }

    private ImmutableSet<IAequivaleoPlugin> plugins = ImmutableSet.of();

    private PluginManger()
    {
    }

    @Override
    public ImmutableSet<IAequivaleoPlugin> getPlugins()
    {
        return plugins;
    }

    public void run(Consumer<IAequivaleoPlugin> callback) {
        StreamUtils.execute(
          () -> getPlugins().parallelStream().forEach(callback)
        );
    }

    public void detect() {
        ModList modList = ModList.get();
        List<IAequivaleoPlugin> plugins = new ArrayList<>();
        for (ModFileScanData scanData : modList.getAllScanData()) {
            for (ModFileScanData.AnnotationData data : scanData.getAnnotations()) {
                if (AEQUIVALEO_PLUGIN_TYPE.equals(data.annotationType())) {
                    final String[] requiredMods = (String[]) data.annotationData().get("requiredMods");
                    if (requiredMods != null && requiredMods.length > 0) {
                        if (Arrays.stream(requiredMods).anyMatch(modId -> !ModList.get().isLoaded(modId))) {
                            continue;
                        }
                    }

                    IAequivaleoPlugin plugin = createPluginFrom(data.memberName());
                    if (plugin != null) {
                        plugins.add(plugin);
                        LOGGER.info("Found and loaded Aequivaleo plugin: {}", plugin.getId());
                    }
                }
            }
        }

        final Collection<Collection<IAequivaleoPlugin>> groupedByIds = GroupingUtils.groupByUsingSet(plugins, IAequivaleoPlugin::getId);
        final Collection<String> idsWithDuplicates = groupedByIds.stream().filter(p -> p.size() > 1).map(p -> p.iterator().next()).map(IAequivaleoPlugin::getId).collect(Collectors.toSet());
        if (idsWithDuplicates.size() > 0) {
            throw new RuntimeException(String.format("Can not load Aequivaleo there are multiple instances of the plugins: [%s]", String.join(", ", idsWithDuplicates)));
        }

        this.plugins = ImmutableSet.copyOf(plugins);
    }

    @Nullable
    private static IAequivaleoPlugin createPluginFrom(String className) {
        return ClassUtils.createOrGetInstance(className, IAequivaleoPlugin.class, AequivaleoPlugin.Instance.class, IAequivaleoPlugin::getId);
    }
}
