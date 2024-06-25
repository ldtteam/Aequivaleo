package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo;

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
     */
    ICoreNode[] flatten();
}
