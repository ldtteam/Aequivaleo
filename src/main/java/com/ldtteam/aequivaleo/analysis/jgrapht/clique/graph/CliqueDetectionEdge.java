package com.ldtteam.aequivaleo.analysis.jgrapht.clique.graph;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IRecipeNode;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Objects;
import java.util.Set;

public class CliqueDetectionEdge extends DefaultWeightedEdge implements IEdge
{
    private final Set<IRecipeNode> recipeNodes;

    public CliqueDetectionEdge(final IRecipeNode recipeNodes) {
        this.recipeNodes = Sets.newHashSet(recipeNodes);
    }

    public CliqueDetectionEdge(final Set<IRecipeNode> recipeNodes) {
        this.recipeNodes = Sets.newHashSet(recipeNodes);
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
    public int hashCode()
    {
        return Objects.hash(getSource(), getTarget());
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof CliqueDetectionEdge))
            return false;
        final CliqueDetectionEdge other = (CliqueDetectionEdge) obj;

        return Objects.equals(getSource(), other.getSource()) &&
                 Objects.equals(getTarget(), other.getTarget());
    }
}
