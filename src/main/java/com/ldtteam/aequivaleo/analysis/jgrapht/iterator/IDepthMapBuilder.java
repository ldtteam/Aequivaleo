package com.ldtteam.aequivaleo.analysis.jgrapht.iterator;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import org.jgrapht.traverse.GraphIterator;

import java.util.Map;

public interface IDepthMapBuilder {
    Map<INode, Integer> calculateDepthMap();
}
