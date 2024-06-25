package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.utils;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.ICoreNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IInnerNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;

import java.util.*;

public class NodeUtils {

    public static ICoreNode[] flatten(INode[] nodes) {
        final List<ICoreNode> flattened = new ArrayList<>();
        final Deque<IInnerNode> innerNodes = new LinkedList<>();

        processNodes(nodes, innerNodes, flattened);

        while(!innerNodes.isEmpty()) {
            final IInnerNode node = innerNodes.pop();
            final INode[] inner = node.nodes();

            processNodes(inner, innerNodes, flattened);
        }

        return flattened.toArray(new ICoreNode[0]);
    }

    private static void processNodes(INode[] nodes, Deque<IInnerNode> innerNodes, List<ICoreNode> flattened) {
        for (INode node : nodes) {
            if (node instanceof IInnerNode innerNode) {
                innerNodes.add(innerNode);
            } else if (node instanceof ICoreNode coreNode) {
                flattened.add(coreNode);
            } else {
                throw new IllegalArgumentException("Unknown node type: " + node.getClass());
            }
        }
    }
}
