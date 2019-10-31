package edu.cmu.cs.mvelezce.instrumenter.graph.block;

import java.util.ArrayList;
import java.util.List;

public class PrettyMethodBlock extends MethodBlock {

  private List<String> prettyInstructions = new ArrayList<>();

  private PrettyMethodBlock(Builder builder) {
    super(builder);
  }

  public List<String> getPrettyInstructions() {
    return this.prettyInstructions;
  }

  public static class Builder extends MethodBlock.Builder {

    public Builder(String id) {
      super(id);
    }

    public PrettyMethodBlock builder() {
      return new PrettyMethodBlock(this);
    }
  }
}
