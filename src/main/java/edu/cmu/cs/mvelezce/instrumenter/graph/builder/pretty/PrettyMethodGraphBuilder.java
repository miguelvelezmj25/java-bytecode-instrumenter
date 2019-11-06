package edu.cmu.cs.mvelezce.instrumenter.graph.builder.pretty;

import edu.cmu.cs.mvelezce.instrumenter.graph.MethodGraph;
import edu.cmu.cs.mvelezce.instrumenter.graph.PrettyMethodGraph;
import edu.cmu.cs.mvelezce.instrumenter.graph.block.MethodBlock;
import edu.cmu.cs.mvelezce.instrumenter.graph.block.PrettyMethodBlock;
import edu.cmu.cs.mvelezce.instrumenter.graph.builder.BaseMethodGraphBuilder;
import edu.cmu.cs.mvelezce.instrumenter.graph.builder.cfg.CFGBuilder;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.util.Printer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PrettyMethodGraphBuilder extends BaseMethodGraphBuilder {

  private PrettyMethodGraph prettyGraph;
  private MethodGraph graph;
  private Printer printer;

  public PrettyMethodGraphBuilder(ClassNode classNode, MethodNode methodNode, Printer printer) {
    super(classNode, methodNode);

    BaseMethodGraphBuilder builder = new CFGBuilder(classNode, methodNode);
    this.graph = builder.build();
    this.printer = printer;
    this.prettyGraph = new PrettyMethodGraph();
  }

  @Override
  public PrettyMethodGraph build() {
    this.addBlocks(this.graph);
    this.addInstructions(this.graph);
    this.addEdges(this.graph);
    this.connectEntryNode(this.graph);
    this.connectExitNode(this.graph);

    return this.prettyGraph;
  }

  @Override
  public void addEdges(MethodGraph graph) {
    for (MethodBlock block : this.graph.getBlocks()) {
      if (block.isSpecial()) {
        continue;
      }

      MethodBlock prettyBlock = this.prettyGraph.getMethodBlock(block.getID());

      for (MethodBlock succ : block.getSuccessors()) {
        MethodBlock prettySucc = this.prettyGraph.getMethodBlock(succ.getID());
        this.prettyGraph.addEdge(prettyBlock, prettySucc);
      }
    }

    for (Map.Entry<MethodBlock, Set<MethodBlock>> exceptionalEdges :
        this.graph.getExceptionalEdges().entrySet()) {
      Set<MethodBlock> exceptionalBlocks = exceptionalEdges.getValue();

      if (exceptionalBlocks.isEmpty()) {
        continue;
      }

      MethodBlock fromBlock = this.prettyGraph.getMethodBlock(exceptionalEdges.getKey().getID());

      for (MethodBlock exceptionalBlock : exceptionalBlocks) {
        MethodBlock toBlock = this.prettyGraph.getMethodBlock(exceptionalBlock.getID());
        this.prettyGraph.addExceptionalEdge(fromBlock, toBlock);
      }
    }
  }

  @Override
  public void addBlocks(MethodGraph graph) {
    for (MethodBlock block : this.graph.getBlocks()) {
      if (block.isSpecial()) {
        continue;
      }

      PrettyMethodBlock prettyBlock = new PrettyMethodBlock.Builder(block.getID()).builder();
      this.prettyGraph.addMethodBlock(prettyBlock);
    }
  }

  @Override
  public void addInstructions(MethodGraph graph) {
    for (MethodBlock block : this.graph.getBlocks()) {
      if (block.isSpecial()) {
        continue;
      }

      List<AbstractInsnNode> blockInstructions = block.getInstructions();
      InsnList methodInstructions = this.getMethodNode().instructions;
      int startIndex = methodInstructions.indexOf(blockInstructions.get(0));
      // Exclusive
      int endIndex = startIndex + blockInstructions.size();

      PrettyMethodBlock prettyBlock =
          (PrettyMethodBlock) this.prettyGraph.getMethodBlock(block.getID());
      List<String> prettyInstructions = prettyBlock.getPrettyInstructions();
      List<Object> printerInstructions = this.printer.getText();

      int offset = 0;

      for (Object string : printerInstructions) {
        if (string.toString().contains("TRYCATCHBLOCK")) {
          offset++;
        } else {
          break;
        }
      }

      if (startIndex != 0) {
        startIndex += offset;
      }

      endIndex += offset;

      for (int i = startIndex; i < endIndex; i++) {
        String instruction = printerInstructions.get(i).toString().trim();
        prettyInstructions.add(instruction);
      }
    }
  }

  @Override
  public void connectEntryNode(MethodGraph graph) {
    MethodBlock entry = this.graph.getEntryBlock();
    MethodBlock prettyEntry = this.prettyGraph.getEntryBlock();

    for (MethodBlock succ : entry.getSuccessors()) {
      MethodBlock prettyBlock = this.prettyGraph.getMethodBlock(succ.getID());
      this.prettyGraph.addEdge(prettyEntry, prettyBlock);
    }
  }

  @Override
  public void connectExitNode(MethodGraph graph) {
    MethodBlock exit = this.graph.getExitBlock();
    MethodBlock prettyExit = this.prettyGraph.getExitBlock();

    for (MethodBlock pred : exit.getPredecessors()) {
      MethodBlock prettyBlock = this.prettyGraph.getMethodBlock(pred.getID());
      this.prettyGraph.addEdge(prettyBlock, prettyExit);
    }
  }
}
