package edu.cmu.cs.mvelezce.instrumenter.graph.builder.cfg;

import edu.cmu.cs.mvelezce.instrumenter.graph.MethodGraph;
import edu.cmu.cs.mvelezce.instrumenter.graph.block.MethodBlock;
import edu.cmu.cs.mvelezce.instrumenter.graph.builder.BaseMethodGraphBuilder;
import edu.cmu.cs.mvelezce.instrumenter.graph.builder.MethodGraphBuilder;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;
import jdk.internal.org.objectweb.asm.tree.analysis.*;

import java.util.*;

public class CFGBuilder extends BaseMethodGraphBuilder {

  // TODO add a new label in a basic block to determine the beginning and end of a control flow
  // decision
  private final String owner;
  private final Map<CFGNode<BasicValue>, Integer> nodesToIndexes = new HashMap<>();
  private Analyzer<BasicValue> analyzer;

  public CFGBuilder(String owner) {
    this.owner = owner;
  }

  public static MethodGraph getCfg(MethodNode methodNode, ClassNode classNode) {
    MethodGraphBuilder cfgBuilder = new CFGBuilder(classNode.name);
    return cfgBuilder.build(methodNode);
  }

  @Override
  public MethodGraph build(MethodNode methodNode) {
    Analyzer<BasicValue> analyzer = this.getASMAnalyzer(methodNode);
    Frame<BasicValue>[] frames = analyzer.getFrames();
    this.cacheNodesToIndex(frames);

    return super.build(methodNode);
  }

  @Override
  public void addBlocks(MethodGraph graph, MethodNode methodNode) {
    InsnList insnList = methodNode.instructions;
    MethodBlock initialBlock = new MethodBlock(insnList.getFirst());
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
    }

    //    for(TryCatchBlockNode tryCatchBlockNode : methodNode.tryCatchBlocks) {
    //      LabelNode start = tryCatchBlockNode.start;
    //      MethodBlock startMethodBlock = graph.getMethodBlock(start);
    //
    //      if(startMethodBlock == null) {
    //        throw new RuntimeException("The start of a try block should be a method block");
    //      }
    //
    //      LabelNode handler = tryCatchBlockNode.handler;
    //      MethodBlock handlerMethodBlock = graph.getMethodBlock(handler);
    //
    //      if(handlerMethodBlock == null) {
    //        throw new RuntimeException("The start of a catch block should be a method block");
    //      }
    //
    //      LabelNode end = tryCatchBlockNode.end;
    //      MethodBlock endMethodBlock = graph.getMethodBlock(end);
    //
    //      if(endMethodBlock == null) {
    //        endMethodBlock = new MethodBlock(end);
    //        graph.addMethodBlock(endMethodBlock);
    //      }
    //    }
  }

  private void AddPredsBlocks(
      MethodGraph graph, CFGNode<BasicValue> cfgNode, AbstractInsnNode insn) {
    Set<CFGNode<BasicValue>> preds = cfgNode.getPredecessors();

    if (preds.size() == 1) {
      return;
    }

    MethodBlock block = graph.getMethodBlock(insn);

    if (block == null) {
      block = new MethodBlock(insn);
      graph.addMethodBlock(block);
    }
  }

  private void addSuccsBlocks(
      MethodGraph graph, CFGNode<BasicValue> cfgNode, InsnList insnList, AbstractInsnNode insn) {
    Set<CFGNode<BasicValue>> succs = cfgNode.getSuccessors();

    if (succs.isEmpty()) {
      if (insn instanceof JumpInsnNode) {
        throw new RuntimeException("A jump instruction does not have any successors!");
      }

      return;
    }

    if (succs.size() == 1 && !(insn instanceof JumpInsnNode)) {
      return;
    }

    if (succs.size() > 1) {
      int curOpcode = insn.getOpcode();

      if (!((curOpcode >= Opcodes.IFEQ & curOpcode <= Opcodes.IF_ACMPNE)
          || curOpcode == Opcodes.IFNULL
          || curOpcode == Opcodes.IFNONNULL)) {
        throw new RuntimeException(
            "The instruction "
                + curOpcode
                + " has multiple successors, but it is not an if comparison");
      }
    }

    for (CFGNode<BasicValue> succ : succs) {
      int succIndex = this.nodesToIndexes.get(succ);
      AbstractInsnNode succInsn = insnList.get(succIndex);
      MethodBlock succBlock = new MethodBlock(succInsn);
      graph.addMethodBlock(succBlock);
    }

    // For non-conditional jumps, the next instruction should be in a separate node
    AbstractInsnNode nextInsn = insn.getNext();
    MethodBlock block = new MethodBlock(nextInsn);
    graph.addMethodBlock(block);
  }

  @Override
  public void addEdges(MethodGraph graph, MethodNode methodNode) {
    Frame<BasicValue>[] frames = this.analyzer.getFrames();
    InsnList insnList = methodNode.instructions;

    MethodBlock entryBlock = graph.getEntryBlock();
    MethodBlock exitBlock = graph.getExitBlock();

    for (MethodBlock block : graph.getBlocks()) {
      if (block == entryBlock || block == exitBlock) {
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
        graph.addEdge(block, succBlock);
      }
    }
  }

  @Override
  public void addInstructions(MethodGraph graph, MethodNode methodNode) {
    List<AbstractInsnNode> curInsnList = null;
    Iterator<AbstractInsnNode> insnIter = methodNode.instructions.iterator();

    while (insnIter.hasNext()) {
      AbstractInsnNode insn = insnIter.next();
      MethodBlock block = graph.getMethodBlock(insn);

      if (block == null) {
        if (curInsnList == null) {
          throw new RuntimeException("The current list of instructions cannot be null");
        }

        curInsnList.add(insn);
      } else {
        curInsnList = block.getInstructions();
        curInsnList.add(insn);
      }
    }
  }

  private Analyzer<BasicValue> getASMAnalyzer(MethodNode methodNode) {
    if (this.analyzer != null) {
      return this.analyzer;
    }

    this.analyzer = new BuildCFGAnalyzer(methodNode);

    try {
      this.analyzer.analyze(this.owner, methodNode);
    } catch (AnalyzerException ae) {
      throw new RuntimeException(
          "Could not build a control flow graph for method " + methodNode.name + methodNode.desc,
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

    protected boolean newControlFlowExceptionEdge(int insnIndex, int successorIndex) {
      return true;
    }

    protected boolean newControlFlowExceptionEdge(int insnIndex, TryCatchBlockNode tryCatchBlock) {
      int handlerIndex = this.instructions.indexOf(tryCatchBlock.handler);

      return this.newControlFlowExceptionEdge(insnIndex, handlerIndex);
    }
  }
}
