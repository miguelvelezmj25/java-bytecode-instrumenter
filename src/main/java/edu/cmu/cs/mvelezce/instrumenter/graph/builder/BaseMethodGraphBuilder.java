package edu.cmu.cs.mvelezce.instrumenter.graph.builder;

import edu.cmu.cs.mvelezce.instrumenter.graph.MethodGraph;
import edu.cmu.cs.mvelezce.instrumenter.graph.block.MethodBlock;
import edu.cmu.cs.mvelezce.instrumenter.graph.exception.InvalidGraphException;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.LabelNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public abstract class BaseMethodGraphBuilder implements MethodGraphBuilder {

  private final ClassNode classNode;
  private final MethodNode methodNode;

  public BaseMethodGraphBuilder(ClassNode classNode, MethodNode methodNode) {
    this.classNode = classNode;
    this.methodNode = methodNode;
  }

  private static boolean isExitMethodInsn(int opcodeLastInsn) {
    return (opcodeLastInsn >= Opcodes.IRETURN && opcodeLastInsn <= Opcodes.RETURN)
        || opcodeLastInsn == Opcodes.RET
        || opcodeLastInsn == Opcodes.ATHROW;
  }

  public ClassNode getClassNode() {
    return classNode;
  }

  public MethodNode getMethodNode() {
    return methodNode;
  }

  @Override
  public MethodGraph build() {
    MethodGraph graph = new MethodGraph();

    if (methodNode.instructions.size() == 0) {
      return graph;
    }

    int instructionsInMethodNodeCount = this.getNumberOfInstructionsInMethodNode();

    this.addBlocks(graph);
    this.addInstructions(graph);

    int instructionsInGraphCount = this.getNumberOfInstructionsInGraph(graph);

    if (instructionsInMethodNodeCount != instructionsInGraphCount) {
      throw new RuntimeException(
          "The number of instructions in the method does not match the total number of"
              + " instructions in the graph");
    }

    this.addEdges(graph);
    this.connectEntryNode(graph);
    this.connectExitNode(graph);
    this.validateGraph(graph);

    return graph;
  }

  private void validateGraph(MethodGraph graph) {
    this.checkEntryAndExitBlocks(graph);
    this.checkSingleReturnInBlock(graph);
    this.checkSuccsAndPreds(graph);
  }

  private void checkSingleReturnInBlock(MethodGraph graph) {
    for (MethodBlock block : graph.getBlocks()) {
      if (block.isSpecial()) {
        continue;
      }

      int returnInsnCount = 0;

      for (AbstractInsnNode insnNode : block.getInstructions()) {
        if (BaseMethodGraphBuilder.isExitMethodInsn(insnNode.getOpcode())) {
          returnInsnCount++;
        }
      }

      if (returnInsnCount > 1) {
        throw new InvalidGraphException(
            graph,
            "The graph for "
                + classNode.name
                + " - "
                + methodNode.name
                + " has a block with more than 1 exit graph instruction");
      }
    }
  }

  private void checkSuccsAndPreds(MethodGraph graph) {
    for (MethodBlock block : graph.getBlocks()) {
      if (block.isSpecial()) {
        continue;
      }

      Set<MethodBlock> succs = block.getSuccessors();
      Set<MethodBlock> preds = block.getPredecessors();

      if (succs.isEmpty() && preds.isEmpty()) {
        throw new InvalidGraphException(graph, "The block " + block.getID() + " is dead code");
      }

      if (succs.isEmpty()) {
        Set<MethodBlock> reachables = graph.getReachableBlocks(graph.getEntryBlock(), block);

        if (reachables.contains(block)) {
          throw new InvalidGraphException(graph, "What is happening?");
        } else {
          throw new InvalidGraphException(graph, "The block " + block.getID() + " has no succs");
        }
      }

      if (preds.isEmpty()) {
        Set<MethodBlock> reachables = graph.getReachableBlocks(block, graph.getExitBlock());

        if (reachables.contains(graph.getExitBlock())) {
          System.err.println("Probably a while true method");
        } else {
          throw new InvalidGraphException(graph, "The block " + block.getID() + " has no pred");
        }
      }
    }
  }

  private void checkEntryAndExitBlocks(MethodGraph graph) {
    if (!graph.getEntryBlock().getPredecessors().isEmpty()) {
      throw new RuntimeException("The entry block has predecessors");
    }

    if (!graph.getExitBlock().getSuccessors().isEmpty()) {
      throw new RuntimeException("The exit block has successors");
    }

    Set<MethodBlock> exitPreds = graph.getExitBlock().getPredecessors();

    for (MethodBlock block : exitPreds) {
      if (!block.isWithReturn()
          && !block.isWithLastInstruction()
          && !block.isWithExplicitThrow() /*&& !graph.isWithWhileTrue()*/) {
        throw new RuntimeException(
            "A block("
                + block.getID()
                + ") connected to the exit block does not have a return instruction");
      }
    }
  }

  private int getNumberOfInstructionsInGraph(MethodGraph graph) {
    int count = 0;

    for (MethodBlock block : graph.getBlocks()) {
      count += block.getInstructions().size();
    }

    return count;
  }

  private int getNumberOfInstructionsInMethodNode() {
    int count = 0;
    ListIterator<AbstractInsnNode> instructionIter = this.methodNode.instructions.iterator();

    while (instructionIter.hasNext()) {
      instructionIter.next();
      count++;
    }

    return count;
  }

  @Override
  public void connectEntryNode(MethodGraph graph) {
    AbstractInsnNode instruction = methodNode.instructions.getFirst();

    if (instruction.getType() != AbstractInsnNode.LABEL) {
      throw new RuntimeException("The first instruction of the method node is not a label.");
    }

    LabelNode labelNode = (LabelNode) instruction;
    MethodBlock firstBlock = graph.getMethodBlock(labelNode);

    graph.addEdge(graph.getEntryBlock(), firstBlock);
  }

  @Override
  public void connectExitNode(MethodGraph graph) {
    this.connectBlocksWithReturn(graph);
    this.connectBlockWithLastInstruction(graph);
    this.connectBlocksWithExplicitThrows(graph);

    //    // TODO do not hard code 3. This can happen if the method has a while(true) loop. Then
    // there is
    //    // no return
    //    if (graph.getBlockCount() == 3) {
    //      for (MethodBlock block : graph.getBlocks()) {
    //        if (block.isWithReturn()) {
    //          return;
    //        }
    //      }
    //
    //      throw new InvalidGraphException(
    //          "There seems to be a special case for graphs with 3 blocks. Test it");
    //      //      Set<MethodBlock> blocks = graph.addBlocks();
    //      //      blocks.remove(graph.getEntryBlock());
    //      //      blocks.remove(graph.getExitBlock());
    //      //
    //      //      graph.addEdge(blocks.iterator().next(), graph.getExitBlock());
    //      //      graph.setWithWhileTrue(true);
    //    }
  }

  private void connectBlocksWithExplicitThrows(MethodGraph graph) {
    for (MethodBlock methodBlock : graph.getBlocks()) {

      if (methodBlock.isSpecial()) {
        continue;
      }

      // TODO find the last instruction of a method block
      for (AbstractInsnNode instruction : methodBlock.getInstructions()) {
        int opcode = instruction.getOpcode();

        if (opcode == Opcodes.ATHROW) {
          methodBlock.setWithExplicitThrow(true);
          graph.addEdge(methodBlock, graph.getExitBlock());

          break;
        }
      }
    }
  }

  private void connectBlockWithLastInstruction(MethodGraph graph) {
    // The last block of a method might not have a return (e.g., ATHROW)
    AbstractInsnNode lastInstruction = methodNode.instructions.getLast();

    for (MethodBlock methodBlock : graph.getBlocks()) {
      if (methodBlock.isSpecial()) {
        continue;
      }

      List<AbstractInsnNode> instructions = methodBlock.getInstructions();

      if (!instructions.contains(lastInstruction)) {
        continue;
      }

      methodBlock.setWithLastInstruction(true);
      graph.addEdge(methodBlock, graph.getExitBlock());
    }
  }

  private void connectBlocksWithReturn(MethodGraph graph) {
    for (MethodBlock methodBlock : graph.getBlocks()) {
      if (methodBlock.isSpecial()) {
        continue;
      }

      // TODO find the last instruction of a method block
      for (AbstractInsnNode instruction : methodBlock.getInstructions()) {
        int opcode = instruction.getOpcode();

        if (opcode == Opcodes.RET || (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
          methodBlock.setWithReturn(true);
          graph.addEdge(methodBlock, graph.getExitBlock());

          break;
        }
      }
    }
  }
}
