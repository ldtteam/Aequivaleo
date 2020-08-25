package com.ldtteam.aequivaleo.recipe.equivalency;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.gameobject.loottable.LootTableAnalyserRegistry;
import net.minecraft.world.server.ServerWorld;

import java.util.Set;

public class DropsEquivalency
    implements IEquivalencyRecipe
{

    private final ICompoundContainer<?> lootTableSource;
    private final boolean isInput;
    private final ServerWorld           world;

    private Set<ICompoundContainer<?>> outputs = null;

    public DropsEquivalency(final ICompoundContainer<?> lootTableSource, final boolean isInput, final ServerWorld world) {
        this.lootTableSource = lootTableSource;
        this.isInput = isInput;
        this.world = world;
        this.outputs = LootTableAnalyserRegistry.getInstance().calculateOutputs(lootTableSource, world);
    }

    @Override
    public Set<ICompoundContainer<?>> getInputs()
    {
        if (isInput)
            return Sets.newHashSet(lootTableSource);

        return outputs;
    }

    @Override
    public Set<ICompoundContainer<?>> getOutputs()
    {
        if (!isInput)
            return Sets.newHashSet(lootTableSource);

        return outputs;
    }

    @Override
    public Double getOffsetFactor()
    {
        return 1d;
    }
}
