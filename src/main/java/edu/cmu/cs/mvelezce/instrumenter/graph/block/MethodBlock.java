package edu.cmu.cs.mvelezce.instrumenter.graph.block;

import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodBlock {

  private String ID;
  private List<AbstractInsnNode> instructions = new ArrayList<>();
  private Set<MethodBlock> successors = new HashSet<>();
  private Set<MethodBlock> predecessors = new HashSet<>();
  private boolean withReturn = false;
  private boolean isHandlerBlock = false;
  private boolean catchWithImplicitThrow = false;
  private boolean withLastInstruction = false;
  private boolean withExplicitThrow = false;

  public MethodBlock(AbstractInsnNode insnNode) {
    this(MethodBlock.asID(insnNode));
  }

  public MethodBlock(String ID) {
    this.ID = ID;
  }

  public static String asID(AbstractInsnNode insnNode) {
    return insnNode.hashCode() + "";
  }

  public boolean isHandlerBlock() {
    return isHandlerBlock;
  }

  public void setHandlerBlock(boolean handlerBlock) {
    isHandlerBlock = handlerBlock;
  }

  public void addSuccessor(MethodBlock methodBlock) {
    successors.add(methodBlock);
  }

  public void addPredecessor(MethodBlock methodBlock) {
    predecessors.add(methodBlock);
  }

  public void reset() {
    this.predecessors.clear();
    this.successors.clear();
  }

  public String getID() {
    return this.ID;
  }

  public List<AbstractInsnNode> getInstructions() {
    return this.instructions;
  }

  public Set<MethodBlock> getSuccessors() {
    return this.successors;
  }

  public Set<MethodBlock> getPredecessors() {
    return this.predecessors;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MethodBlock that = (MethodBlock) o;

    return ID.equals(that.ID);
  }

  @Override
  public int hashCode() {
    return ID.hashCode();
  }

  @Override
  public String toString() {
    return this.ID;
  }

  public boolean isWithReturn() {
    return withReturn;
  }

  public void setWithReturn(boolean withReturn) {
    this.withReturn = withReturn;
  }

  public boolean isCatchWithImplicitThrow() {
    return this.catchWithImplicitThrow;
  }

  public void setCatchWithImplicitThrow(boolean catchWithImplicitThrow) {
    this.catchWithImplicitThrow = catchWithImplicitThrow;
  }

  public boolean isWithLastInstruction() {
    return withLastInstruction;
  }

  public void setWithLastInstruction(boolean withLastInstruction) {
    this.withLastInstruction = withLastInstruction;
  }

  public boolean isWithExplicitThrow() {
    return withExplicitThrow;
  }

  public void setWithExplicitThrow(boolean withExplicitThrow) {
    this.withExplicitThrow = withExplicitThrow;
  }
}
