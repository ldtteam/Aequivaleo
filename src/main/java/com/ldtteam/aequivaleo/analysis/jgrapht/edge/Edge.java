package com.ldtteam.aequivaleo.analysis.jgrapht.edge;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Objects;
import java.util.Optional;

public class Edge extends DefaultWeightedEdge implements IEdge
{
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Integer> hashCode = Optional.empty();

    private final INode source;
    private final INode target;

    public Edge(final INode source, final INode target)
    {
        this.source = source;
        this.target = target;
    }

    @Override
    public INode getSource() {
        return source;
    }

    @Override
    public INode getTarget() {
        return target;
    }

    @Override
    public double getWeight()
    {
        return super.getWeight();
    }

    @Override
    public int hashCode()
    {
        if (this.hashCode.isEmpty() &&
            getSource() != null &&
              getTarget() != null
        ) {
            this.hashCode = Optional.of(
              Objects.hash(getSource(), getTarget(), getWeight())
            );
        }

        return this.hashCode.orElseGet(() -> Objects.hash(getSource(), getTarget(), getWeight()));
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof Edge other))
            return false;

        return Objects.equals(getSource(), other.getSource()) &&
                 Objects.equals(getTarget(), other.getTarget()) &&
                 Objects.equals(getWeight(), other.getWeight());
    }

    @Override
    public String toString() {
        return "(" + getSource() + " : " + getTarget() + ")";
    }
}
