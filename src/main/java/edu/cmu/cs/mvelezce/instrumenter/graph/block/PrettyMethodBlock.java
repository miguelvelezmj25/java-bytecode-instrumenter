package edu.cmu.cs.mvelezce.instrumenter.graph.block;

import java.util.ArrayList;
import java.util.List;

public class PrettyMethodBlock extends MethodBlock {

  private List<String> prettyInstructions = new ArrayList<>();

  public PrettyMethodBlock(String ID) {
    super(ID);
  }

  public List<String> getPrettyInstructions() {
    return this.prettyInstructions;
  }
}
