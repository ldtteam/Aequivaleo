package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo;

import java.util.Iterator;

/**
 * Represents a graph node which has other nodes contained in itself.
 */
public interface IInnerNode extends ISimulateableNode
{

    /**
     * @return The nodes contained in this node.
     */
    INode[] nodes();

    /**
     * @return The {@link ICoreNode} contained in this node and all of its children
     * @implSpec Default implementation flattens the node using a recursive iterator.
     */
    Iterator<ICoreNode> flatten();

    /**
     * @return Whether this node or any of its children have results.
     * @implSpec Default implementation flattens the node and checks if any of the nodes have results.
     */
    @Override
    default boolean canPropagate() {
        final Iterator<ICoreNode> flatten = flatten();
        while (flatten.hasNext()) {
            if (flatten.next() instanceof IResultsOwningNode resultsOwningNode && resultsOwningNode.canPropagate()) {
                return true;
            }
        }

        return false;
    }
}
