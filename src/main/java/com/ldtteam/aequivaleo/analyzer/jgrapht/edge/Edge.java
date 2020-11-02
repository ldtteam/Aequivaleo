package com.ldtteam.aequivaleo.analyzer.jgrapht.edge;

import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Objects;
import java.util.Optional;

public class Edge extends DefaultWeightedEdge implements IEdge
{

    private Optional<Integer> hashCode = Optional.empty();

    public Edge()
    {
    }

    @Override
    public double getWeight()
    {
        return super.getWeight();
    }

    @Override
    public int hashCode()
    {
        if (!this.hashCode.isPresent() &&
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
        if (!(obj instanceof Edge))
            return false;
        final Edge other = (Edge) obj;

        return Objects.equals(getSource(), other.getSource()) &&
                 Objects.equals(getTarget(), other.getTarget()) &&
                 Objects.equals(getWeight(), other.getWeight());
    }
}
