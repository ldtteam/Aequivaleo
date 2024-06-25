package com.ldtteam.aequivaleo.analysis.jgrapht.clique.graph;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IRecipeNode;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Objects;
import java.util.Set;

public class CliqueDetectionEdge<N> extends DefaultWeightedEdge implements IEdge
{

    private final Set<N> intermediaryNodes;

    public CliqueDetectionEdge(Set<N> intermediaryNodes) {
        this.intermediaryNodes = intermediaryNodes;
    }

    @Override
    public double getWeight()
    {
        return 1;
    }

    public Set<N> getIntermediaryNodes()
    {
        return intermediaryNodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CliqueDetectionEdge<?> that = (CliqueDetectionEdge<?>) o;
        return Objects.equals(getIntermediaryNodes(), that.getIntermediaryNodes());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getIntermediaryNodes());
    }

    @Override
    public String toString() {
        return "CliqueDetectionEdge{" +
                "intermediaryNodes=" + intermediaryNodes +
                '}';
    }
}
