package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;

public class ResultsAlreadyPolledException extends RuntimeException {

    public ResultsAlreadyPolledException(INode node) {
        super("Results have already been polled on node " + node + ". This is not allowed.");
    }
}
