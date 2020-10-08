package com.ldtteam.aequivaleo.analyzer.jgrapht.visual;

import com.ldtteam.aequivaleo.analyzer.jgrapht.node.IAnalysisGraphNode;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;

public class RecipeGraphVisualizationApplet extends JApplet
{
    private final Graph<IAnalysisGraphNode, DefaultWeightedEdge> graph;

    private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

    private JGraphXAdapter<IAnalysisGraphNode, DefaultWeightedEdge> jgxAdapter;

    public RecipeGraphVisualizationApplet(final Graph<IAnalysisGraphNode, DefaultWeightedEdge> graph) {this.graph = graph;}

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

    /**
     * Called by the browser or applet viewer to inform
     * this applet that it has been loaded into the system. It is always
     * called before the first time that the <code>start</code> method is
     * called.
     * <p>
     * A subclass of <code>Applet</code> should override this method if
     * it has initialization to perform. For example, an applet with
     * threads would use the <code>init</code> method to create the
     * threads and the <code>destroy</code> method to kill them.
     * <p>
     * The implementation of this method provided by the
     * <code>Applet</code> class does nothing.
     *
     * @see Applet#destroy()
     * @see Applet#start()
     * @see Applet#stop()
     */
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
