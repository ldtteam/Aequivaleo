package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.utils;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.ICoreNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IInnerNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;

import java.util.*;

public class NodeUtils {

    public static int nodeCount(Iterable<INode> nodes) {
        int count = 0;
        for (INode node : nodes) {
            count++;
            if (node instanceof IInnerNode innerNode) {
                count += nodeCount(Arrays.asList(innerNode.nodes()));
            }
        }
        return count;
    }

    public static Iterator<ICoreNode> flatten(INode[] nodes) {
        return new Iterator<>() {

            private int nextIndex = 0;
            private Iterator<ICoreNode> innerIterator = null;

            @Override
            public boolean hasNext() {
                return innerIterator == null || innerIterator.hasNext() || nextIndex < nodes.length;
            }

            @Override
            public ICoreNode next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                if (innerIterator == null || !innerIterator.hasNext()) {
                    final INode nextNode = nodes[nextIndex];
                    if (nextNode instanceof IInnerNode innerNode) {
                        innerIterator = flatten(innerNode.nodes());
                    } else if (nextNode instanceof ICoreNode coreNode) {
                        innerIterator = Collections.singleton(coreNode).iterator();
                    } else {
                        throw new IllegalArgumentException("Unknown node type: " + nextNode.getClass());
                    }
                    nextIndex++;
                }
                return innerIterator.next();
            }
        };
    }
}
