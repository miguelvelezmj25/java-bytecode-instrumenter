package edu.cmu.cs.mvelezce.instrumenter.graph.builder.cfg;

import jdk.internal.org.objectweb.asm.tree.analysis.Frame;
import jdk.internal.org.objectweb.asm.tree.analysis.Value;

import java.util.HashSet;
import java.util.Set;

class CFGNode<V extends Value> extends Frame<V> {

  private final Set<CFGNode<V>> successors = new HashSet<>();
  private final Set<CFGNode<V>> predecessors = new HashSet<>();

  CFGNode(int nLocals, int nStack) {
    super(nLocals, nStack);
  }

  CFGNode(Frame<? extends V> src) {
    super(src);
  }

  Set<CFGNode<V>> getSuccessors() {
    return successors;
  }

  Set<CFGNode<V>> getPredecessors() {
    return predecessors;
  }
}
