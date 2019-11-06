package edu.cmu.cs.mvelezce.instrumenter.graph.builder;

import edu.cmu.cs.mvelezce.instrumenter.graph.MethodGraph;

public interface MethodGraphBuilder {

  MethodGraph build();

  void addEdges(MethodGraph graph);

  void addBlocks(MethodGraph graph);

  void addInstructions(MethodGraph graph);

  void connectEntryNode(MethodGraph graph);

  void connectExitNode(MethodGraph graph);
}
