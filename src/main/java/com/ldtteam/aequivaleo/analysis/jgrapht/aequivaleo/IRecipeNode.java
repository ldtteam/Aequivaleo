package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A marker interface for nodes which represent a recipe.
 */
public interface IRecipeNode extends INode
{

    /**
     * Gives access to the recipe that this node represents.
     *
     * @return The recipe.
     */
    @NotNull
    IEquivalencyRecipe getRecipe();

    @Override
    default boolean hasParentsWithMissingData(IGraph graph, ICompoundTypeGroup group)
    {
        if (!graph.containsVertex(this))
        {
            return false;
        }
        for (IEdge iEdge : graph.incomingEdgesOf(this))
        {
            INode node = graph.getEdgeSource(iEdge);
            if (node instanceof IRecipeInputNode) {
                final IRecipeInputNode inputNode = (IRecipeInputNode) node;
                final Set<IRecipeInputNode> inputNodes = inputNode.getInputNodes(this);
                for (final IRecipeInputNode directInputNode : inputNodes)
                {
                    if (directInputNode.hasMissingData(graph, group))
                    {
                        return true;
                    }
                }
            }
            else if (node instanceof IRecipeResidueNode) {
                final IRecipeResidueNode inputNode = (IRecipeResidueNode) node;
                final Set<CompoundInstance> instances = inputNode.getResidueInstances(this);
                boolean hasNoMatchingGroup = true;
                for (CompoundInstance instance : instances)
                {
                    ICompoundTypeGroup g = instance.getGroup();
                    if (g.equals(group))
                    {
                        hasNoMatchingGroup = false;
                        break;
                    }
                }
                if (hasNoMatchingGroup) {
                    return true;
                }
            }
        }
        return false;
    }
}
