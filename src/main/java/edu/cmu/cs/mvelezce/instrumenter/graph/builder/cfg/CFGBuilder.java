package edu.cmu.cs.mvelezce.instrumenter.graph.builder.cfg;

import edu.cmu.cs.mvelezce.instrumenter.graph.MethodGraph;
import edu.cmu.cs.mvelezce.instrumenter.graph.block.MethodBlock;
import edu.cmu.cs.mvelezce.instrumenter.graph.builder.BaseMethodGraphBuilder;
import edu.cmu.cs.mvelezce.instrumenter.graph.builder.MethodGraphBuilder;
import jdk.internal.org.objectweb.asm.tree.*;
import jdk.internal.org.objectweb.asm.tree.analysis.*;

import java.util.*;

public class CFGBuilder extends BaseMethodGraphBuilder {

  // TODO add a new label in a basic block to determine the beginning and end of a control flow
  // decision
  private final Map<CFGNode<BasicValue>, Integer> nodesToIndexes = new HashMap<>();
  private Analyzer<BasicValue> analyzer;

  public CFGBuilder(ClassNode classNode, MethodNode methodNode) {
    super(classNode, methodNode);
  }

  public static MethodGraph getCfg(MethodNode methodNode, ClassNode classNode) {
    MethodGraphBuilder cfgBuilder = new CFGBuilder(classNode, methodNode);
    return cfgBuilder.build();
  }

  @Override
  public MethodGraph build() {
    Analyzer<BasicValue> analyzer = this.getASMAnalyzer();
    Frame<BasicValue>[] frames = analyzer.getFrames();
    this.cacheNodesToIndex(frames);

    return super.build();
  }

  @Override
  public void addBlocks(MethodGraph graph) {
    InsnList insnList = this.getMethodNode().instructions;
    MethodBlock initialBlock = new MethodBlock.Builder(insnList.getFirst()).build();
    graph.addMethodBlock(initialBlock);

    Frame<BasicValue>[] frames = this.analyzer.getFrames();

    for (int i = 1; i < frames.length; i++) {
      Frame<BasicValue> frame = frames[i];

      if (frame == null) {
        continue;
      }

      CFGNode<BasicValue> cfgNode = (CFGNode<BasicValue>) frame;
      AbstractInsnNode insn = insnList.get(i);

      this.AddPredsBlocks(graph, cfgNode, insn);
      this.addSuccsBlocks(graph, cfgNode, insnList, insn);
      this.addExceptionalBlocks(graph);
    }
  }

  private void addExceptionalBlocks(MethodGraph graph) {
    for (TryCatchBlockNode tryCatchBlockNode : this.getMethodNode().tryCatchBlocks) {
      LabelNode start = tryCatchBlockNode.start;
      MethodBlock startBlock = new MethodBlock.Builder(start).build();
      graph.addMethodBlock(startBlock);

      LabelNode end = tryCatchBlockNode.end;
      MethodBlock endBlock = new MethodBlock.Builder(end).build();
      graph.addMethodBlock(endBlock);

      LabelNode handler = tryCatchBlockNode.handler;
      MethodBlock handlerBlock = new MethodBlock.Builder(handler).build();
      graph.addMethodBlock(handlerBlock);
    }
  }

  private void AddPredsBlocks(
      MethodGraph graph, CFGNode<BasicValue> cfgNode, AbstractInsnNode insn) {
    Set<CFGNode<BasicValue>> preds = cfgNode.getPredecessors();

    if (preds.size() == 1) {
      return;
    }

    MethodBlock block = graph.getMethodBlock(insn);

    if (block == null) {
      block = new MethodBlock.Builder(insn).build();
      graph.addMethodBlock(block);
    }
  }

  private void addSuccsBlocks(
      MethodGraph graph, CFGNode<BasicValue> cfgNode, InsnList insnList, AbstractInsnNode insn) {
    Set<CFGNode<BasicValue>> succs = cfgNode.getSuccessors();

    if (succs.isEmpty()) {
      if (insn instanceof JumpInsnNode
          || insn instanceof TableSwitchInsnNode
          || insn instanceof LookupSwitchInsnNode) {
        throw new RuntimeException("A jump or switch instruction does not have any successors!");
      }

      return;
    }

    if (succs.size() == 1 && !(insn instanceof JumpInsnNode)) {
      return;
    }

    for (CFGNode<BasicValue> succ : succs) {
      int succIndex = this.nodesToIndexes.get(succ);
      AbstractInsnNode succInsn = insnList.get(succIndex);
      MethodBlock succBlock = new MethodBlock.Builder(succInsn).build();
      graph.addMethodBlock(succBlock);
    }

    AbstractInsnNode nextInsn = insn.getNext();
    MethodBlock block = new MethodBlock.Builder(nextInsn).build();
    graph.addMethodBlock(block);
  }

  @Override
  public void addEdges(MethodGraph graph) {
    Frame<BasicValue>[] frames = this.analyzer.getFrames();
    InsnList insnList = this.getMethodNode().instructions;

    for (MethodBlock block : graph.getBlocks()) {
      if (block.isSpecial()) {
        continue;
      }

      List<AbstractInsnNode> blockInsnList = block.getInstructions();
      AbstractInsnNode lastInsn = blockInsnList.get(blockInsnList.size() - 1);
      int lastInsnIndex = insnList.indexOf(lastInsn);
      CFGNode<BasicValue> node = (CFGNode<BasicValue>) frames[lastInsnIndex];

      if (node == null) {
        continue;
      }

      Set<CFGNode<BasicValue>> succs = node.getSuccessors();

      for (CFGNode<BasicValue> succ : succs) {
        int succIndex = this.nodesToIndexes.get(succ);
        AbstractInsnNode succInsn = insnList.get(succIndex);
        MethodBlock succBlock = graph.getMethodBlock(succInsn);

        if (succBlock == null) {
          throw new RuntimeException("The successor block is null");
        }

        if (succBlock.isHandlerBlock()) {
          throw new RuntimeException("Did not expect the successor node to be a handler block");
        }

        graph.addEdge(block, succBlock);
      }
    }

    this.addExceptionalEdges(graph);
  }

  private void addExceptionalEdges(MethodGraph graph) {
    for (TryCatchBlockNode tryCatchBlockNode : this.getMethodNode().tryCatchBlocks) {
      AbstractInsnNode handler = tryCatchBlockNode.handler;
      MethodBlock handlerBlock = graph.getMethodBlock(handler);

      if (handlerBlock == null) {
        throw new RuntimeException("Did not expect the block to be null");
      }

      if (handlerBlock.getPredecessors().isEmpty()) {
        handlerBlock.setCatchWithImplicitThrow(true);
      }

      LabelNode start = tryCatchBlockNode.start;
      MethodBlock methodBlock = graph.getMethodBlock(start);

      if (methodBlock == null) {
        throw new RuntimeException("Did not expect the block to be null");
      }

      graph.addExceptionalEdge(methodBlock, handlerBlock);

      // Add edge from all blocks within start and end (exclusive) to the handler
      AbstractInsnNode insnNode = start.getNext();

      while (!tryCatchBlockNode.end.equals(insnNode)) {
        methodBlock = graph.getMethodBlock(insnNode);

        if (methodBlock != null) {
          graph.addExceptionalEdge(methodBlock, handlerBlock);
        }

        insnNode = insnNode.getNext();
      }
    }
  }

  @Override
  public void addInstructions(MethodGraph graph) {
    List<AbstractInsnNode> curInsnList = null;
    Iterator<AbstractInsnNode> insnIter = this.getMethodNode().instructions.iterator();

    Set<AbstractInsnNode> handlerInstructions = new HashSet<>();

    for (TryCatchBlockNode tryCatchBlockNode : this.getMethodNode().tryCatchBlocks) {
      handlerInstructions.add(tryCatchBlockNode.handler);
    }

    while (insnIter.hasNext()) {
      AbstractInsnNode insn = insnIter.next();
      MethodBlock block = graph.getMethodBlock(insn);

      if (block == null) {
        if (curInsnList == null) {
          throw new RuntimeException("The current list of instructions cannot be null");
        }

        curInsnList.add(insn);
      } else {
        if (handlerInstructions.contains(insn)) {
          block.setHandlerBlock(true);
        }

        curInsnList = block.getInstructions();
        curInsnList.add(insn);
      }
    }
  }

  private Analyzer<BasicValue> getASMAnalyzer() {
    if (this.analyzer != null) {
      return this.analyzer;
    }

    this.analyzer = new BuildCFGAnalyzer(this.getMethodNode());

    try {
      this.analyzer.analyze(this.getMethodNode().name, this.getMethodNode());
    } catch (AnalyzerException ae) {
      throw new RuntimeException(
          "Could not build a control flow graph for method "
              + this.getMethodNode().name
              + this.getMethodNode().desc,
          ae);
    }

    return this.analyzer;
  }

  private Map<CFGNode<BasicValue>, Integer> cacheNodesToIndex(Frame<BasicValue>[] frames) {
    if (!this.nodesToIndexes.isEmpty()) {
      return this.nodesToIndexes;
    }

    for (int i = 0; i < frames.length; i++) {
      CFGNode<BasicValue> cfgNode = (CFGNode<BasicValue>) frames[i];
      this.nodesToIndexes.put(cfgNode, i);
    }

    return this.nodesToIndexes;
  }

  private static class BuildCFGAnalyzer extends Analyzer<BasicValue> {

    private InsnList instructions;

    BuildCFGAnalyzer(MethodNode methodNode) {
      super(new BasicInterpreter());

      this.instructions = methodNode.instructions;
    }

    @Override
    protected Frame<BasicValue> newFrame(int nLocals, int nStack) {
      return new CFGNode<>(nLocals, nStack);
    }

    @Override
    protected Frame<BasicValue> newFrame(Frame<? extends BasicValue> src) {
      return new CFGNode<>(src);
    }

    @Override
    protected void newControlFlowEdge(int src, int dst) {
      Frame<BasicValue>[] frames = this.getFrames();

      CFGNode<BasicValue> srcNode = (CFGNode<BasicValue>) frames[src];
      CFGNode<BasicValue> dstNode = (CFGNode<BasicValue>) frames[dst];

      srcNode.getSuccessors().add(dstNode);
      dstNode.getPredecessors().add(srcNode);
    }

    //    protected boolean newControlFlowExceptionEdge(int insnIndex, int successorIndex) {
    //      return super.newControlFlowExceptionEdge(insnIndex, successorIndex);
    //      //      return false;
    //    }
    //
    //    protected boolean newControlFlowExceptionEdge(int insnIndex, TryCatchBlockNode
    // tryCatchBlock) {
    //      return super.newControlFlowExceptionEdge(insnIndex, tryCatchBlock);
    //      //      return false;
    //
    //      //            int handlerIndex = this.instructions.indexOf(tryCatchBlock.handler);
    //      //
    //      //            return this.newControlFlowExceptionEdge(insnIndex, handlerIndex);
    //    }
  }
}
