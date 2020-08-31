package com.ldtteam.aequivaleo.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;

public interface IInstancedEquivalency extends IEquivalencyRecipe
{
    boolean isBlock();

    ICompoundContainer<?> getSource();

    ICompoundContainer<?> getTarget();
}
