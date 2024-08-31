package com.ldtteam.aequivaleo.bootstrap;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.analysis.EquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.information.ICompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.api.util.StreamUtils;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.information.CompoundInformationRegistry;
import com.ldtteam.aequivaleo.instanced.InstancedEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.recipe.equivalency.InstancedEquivalency;
import com.ldtteam.aequivaleo.recipe.equivalency.TagEquivalencyRecipe;
import com.ldtteam.aequivaleo.vanilla.tags.TagEquivalencyRegistry;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.StreamSupport;

@SuppressWarnings("rawtypes")
public final class WorldBootstrapper {

    private static final Logger LOGGER = LogManager.getLogger();

    private WorldBootstrapper() {
        throw new IllegalStateException("Tried to initialize: WorldBootstrapper but this is a Utility class.");
    }

    public static void onWorldReload(final Collection<ServerLevel> levels) {
        resetDataForWorld(levels);

        doBootstrapTagInformation(levels);
        doBootstrapInstancedEquivalencies(levels);

        doHandleCompoundTypeWrappers(levels);

        doHandlePluginLoad(levels);
    }

    private static void resetDataForWorld(final Collection<ServerLevel> levels) {
        for (ServerLevel world : levels) {
            LOGGER.info(String.format("Resetting data for world: %s", world.dimension().location()));
            CompoundInformationRegistry.getInstance(world.dimension()).reset();
            EquivalencyRecipeRegistry.getInstance(world.dimension()).reset();
        }
    }

    private static void doBootstrapTagInformation(final Collection<ServerLevel> levels) {
        for (TagKey<?> tag : TagEquivalencyRegistry.getInstance().getTags()) {
            doBootstrapSingleTagInformation(levels, tag);
        }
    }

    private static <T> void doBootstrapSingleTagInformation(final Collection<ServerLevel> levels, final TagKey<T> tag) {
        Validate.notEmpty(levels, "Can not bootstrap tag information without any levels.");

        final ICompoundContainer<TagKey> tagContainer = CompoundContainerFactoryManager.getInstance().wrapInContainer(tag, 1d);
        final ServerLevel primary = levels.iterator().next();

        final Collection<ICompoundContainer<?>> elementsOfTag = new ArrayList<>();
        for (Holder<T> stack : primary.registryAccess().registryOrThrow(tag.registry()).getOrCreateTag(tag)) {
            ICompoundContainer<T> tiCompoundContainer = CompoundContainerFactoryManager.getInstance().wrapInContainer(stack.value(), 1d);
            elementsOfTag.add(tiCompoundContainer);
        }

        for (ICompoundContainer<?> inputStack : elementsOfTag) {
            final TagEquivalencyRecipe<T> fromTagToStack = new TagEquivalencyRecipe<>(
                    tag,
                    tagContainer,
                    inputStack
            );
            final TagEquivalencyRecipe<T> fromStackToTag = new TagEquivalencyRecipe<>(
                    tag,
                    inputStack,
                    tagContainer
            );

            for (ServerLevel level : levels) {
                EquivalencyRecipeRegistry.getInstance(level.dimension())
                        .register(fromTagToStack)
                        .register(fromStackToTag);
            }
        }
    }

    private static void doBootstrapInstancedEquivalencies(
            @NotNull final Collection<ServerLevel> levels
    ) {
        StreamUtils.execute(() -> {
            StreamSupport.stream(ForgeRegistries.ITEMS.spliterator(), true)
                    .filter(item -> !item.equals(Items.AIR))
                    .forEach(item -> InstancedEquivalencyHandlerRegistry.getInstance().process(
                            item,
                            o -> {
                                final ICompoundContainer<?> sourceContainer = CompoundContainerFactoryManager.getInstance().wrapInContainer(item, 1);
                                final ICompoundContainer<?> targetContainer = CompoundContainerFactoryManager.getInstance().wrapInContainer(o, 1);

                                final InstancedEquivalency sourceToTarget = new InstancedEquivalency(
                                        sourceContainer, targetContainer
                                );
                                final InstancedEquivalency targetToSource = new InstancedEquivalency(
                                        targetContainer, sourceContainer
                                );

                                try {
                                    for (ServerLevel level : levels) {
                                        EquivalencyRecipeRegistry.getInstance(level.dimension())
                                                .register(sourceToTarget)
                                                .register(targetToSource);
                                    }
                                } catch (Exception ex) {
                                    LOGGER.error(String.format("Failed to register equivalency between: %s and: %s",
                                            ForgeRegistries.ITEMS.getKey(item),
                                            o), ex);

                                }
                            },
                            consumer -> consumer.accept(item.getDefaultInstance())
                    ));

            StreamSupport.stream(ForgeRegistries.FLUIDS.spliterator(), true).forEach(fluid -> InstancedEquivalencyHandlerRegistry.getInstance().process(
                    fluid,
                    o -> {
                        final ICompoundContainer<?> sourceContainer = CompoundContainerFactoryManager.getInstance().wrapInContainer(fluid, 1);
                        final ICompoundContainer<?> targetContainer = CompoundContainerFactoryManager.getInstance().wrapInContainer(o, 1);

                        final InstancedEquivalency sourceToTarget = new InstancedEquivalency(
                                sourceContainer, targetContainer
                        );
                        final InstancedEquivalency targetToSource = new InstancedEquivalency(
                                targetContainer, sourceContainer
                        );

                        try {
                            for (ServerLevel level : levels) {
                                EquivalencyRecipeRegistry.getInstance(level.dimension())
                                        .register(sourceToTarget)
                                        .register(targetToSource);
                            }
                        } catch (Exception ex) {
                            LOGGER.error(String.format("Failed to register equivalency between: %s and: %s",
                                    ForgeRegistries.FLUIDS.getKey(fluid),
                                    o), ex);
                        }
                    },
                    consumer -> {
                        if (fluid.isSame(Fluids.EMPTY))
                            return;

                        consumer.accept(new FluidStack(fluid, 1));
                    }
            ));
        });
    }

    private static void doHandleCompoundTypeWrappers(
            @NotNull final Collection<ServerLevel> levels) {
        for (ServerLevel level : levels) {
            LOGGER.info(String.format("Setting up compound type instantiations: %s", level.dimension().location()));
            ModRegistries.COMPOUND_TYPE.get().forEach(type -> ICompoundInformationRegistry.getInstance(level.dimension())
                    .registerLocking(type, Sets.newHashSet(new CompoundInstance(type, 1))));
        }
    }

    private static void doHandlePluginLoad(
            @NotNull final Collection<ServerLevel> levels) {
        for (ServerLevel level : levels) {
            LOGGER.info(String.format("Invoking plugin callbacks: %s", level.dimension().location()));
            PluginManger.getInstance().run(plugin -> plugin.onReloadStartedFor(level));
        }

        LOGGER.info(String.format("Invoking plugin callbacks: %s", levels));
        PluginManger.getInstance().run(plugin -> plugin.onReloadStartedFor(levels));
    }
}
