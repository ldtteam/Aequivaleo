package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo;

import java.util.Collection;

/**
 * Represents a node that represents a core component of a recipe graph and not a structural component of the graph.
 * <p>
 *     When a recipe graph is build it will originally only contain core nodes. These nodes are then potentially
 *     transformed into other nodes to deal with structural defects in the graph. Examples of such defects are
 *     cycles or cliques.
 * </p>
 */
public interface ICoreNode extends INode {

    /**
     * Adds an input to this node.
     *
     * @param input The input node.
     * @param weight The weight of the input.
     */
    void addInput(ICoreNode input, double weight);

    /**
     * Adds an output to this node.
     *
     * @param output The output node.
     * @param weight The weight of the output.
     */
    void addOutput(ICoreNode output, double weight);

    /**
     * Gets the inputs of this node.
     *
     * @return The inputs.
     */
    Collection<? extends ICoreNode> inputs();

    /**
     * Gets the outputs of this node.
     *
     * @return The outputs.
     */
    Collection<? extends ICoreNode> outputs();

    /**
     * Removes an input from this node.
     *
     * @param input The input node.
     */
    void removeOutput(ICoreNode input);

    /**
     * Removes all inputs from this node.
     */
    void clearInputs();
}
