package com.ldtteam.aequivaleo.analyzer.jgrapht.visual;

import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.Edge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class RecipeGraphVisualizationApplet extends JApplet
{
    private final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, Edge> graph;

    private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

    private JGraphXAdapter<IAnalysisGraphNode<Set<CompoundInstance>>, Edge> jgxAdapter;

    public RecipeGraphVisualizationApplet(final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, Edge> graph) {this.graph = graph;}

    public void run()
    {
        SwingUtilities.invokeLater(() -> {
            init();

            JFrame frame = new JFrame();
            frame.getContentPane().add(this);
            frame.setTitle("JGraphT Adapter to JGraphX Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }

    @Override
    public void init()
    {
        jgxAdapter = new JGraphXAdapter<>(graph);

        setPreferredSize(DEFAULT_SIZE);
        mxGraphComponent component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);
        getContentPane().add(component);
        resize(DEFAULT_SIZE);

        // positioning via jgraphx layouts
        mxCompactTreeLayout layout = new mxCompactTreeLayout(jgxAdapter);
        layout.setHorizontal(false);
        layout.setLevelDistance(10);
        layout.setNodeDistance(100);
        layout.setEdgeRouting(true);

        layout.execute(jgxAdapter.getDefaultParent());
    }
}
