package com.ldtteam.aequivaleo.analysis.jgrapht.clique.graph;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IRecipeNode;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Objects;
import java.util.Set;

public class CliqueDetectionEdge extends DefaultWeightedEdge implements IEdge
{

    private final Set<INode> intermediaryNodes;

    public CliqueDetectionEdge(Set<INode> intermediaryNodes) {
        this.intermediaryNodes = intermediaryNodes;
    }

    @Override
    public double getWeight()
    {
        return 1;
    }

    public Set<INode> getIntermediaryNodes()
    {
        return intermediaryNodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CliqueDetectionEdge that = (CliqueDetectionEdge) o;
        return Objects.equals(intermediaryNodes, that.intermediaryNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intermediaryNodes);
    }

    @Override
    public String toString() {
        return "CliqueDetectionEdge{" +
                "intermediaryNodes=" + intermediaryNodes +
                '}';
    }
}
