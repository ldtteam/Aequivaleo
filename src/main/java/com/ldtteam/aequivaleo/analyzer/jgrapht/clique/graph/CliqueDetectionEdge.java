package com.ldtteam.aequivaleo.analyzer.jgrapht.clique.graph;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IRecipeNode;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class CliqueDetectionEdge implements IEdge
{
    private static AtomicLong EDGE_ID_GEN = new AtomicLong();

    private final long             id;
    private final Set<IRecipeNode> recipeNodes;

    public CliqueDetectionEdge(final IRecipeNode recipeNodes) {
        this.id = EDGE_ID_GEN.getAndIncrement();
        this.recipeNodes = Sets.newHashSet(recipeNodes);
    }

    public CliqueDetectionEdge(final Set<IRecipeNode> recipeNodes) {
        this.id = EDGE_ID_GEN.getAndIncrement();
        this.recipeNodes = Sets.newHashSet(recipeNodes);
    }

    @Override
    public long getEdgeIdentifier()
    {
        return id;
    }

    @Override
    public double getWeight()
    {
        return 1;
    }

    public Set<IRecipeNode> getRecipeNodes()
    {
        return recipeNodes;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof CliqueDetectionEdge))
        {
            return false;
        }
        final CliqueDetectionEdge that = (CliqueDetectionEdge) o;
        return id == that.id;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
}
