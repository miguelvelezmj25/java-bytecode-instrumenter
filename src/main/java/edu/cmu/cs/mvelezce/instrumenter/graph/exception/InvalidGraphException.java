package edu.cmu.cs.mvelezce.instrumenter.graph.exception;

import edu.cmu.cs.mvelezce.instrumenter.graph.MethodGraph;

public class InvalidGraphException extends RuntimeException {

  public InvalidGraphException(MethodGraph graph, String message) {
    super(message + "\n");

    System.err.println(graph.toDotString("error"));
  }
}
