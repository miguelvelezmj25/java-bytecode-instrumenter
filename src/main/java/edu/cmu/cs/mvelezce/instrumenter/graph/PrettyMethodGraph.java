package edu.cmu.cs.mvelezce.instrumenter.graph;

import com.mijecu25.meme.utils.execute.Executor;
import edu.cmu.cs.mvelezce.instrumenter.graph.block.MethodBlock;
import edu.cmu.cs.mvelezce.instrumenter.graph.block.PrettyMethodBlock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class PrettyMethodGraph extends MethodGraph {

  private static final String DOT_DOT = ".dot";
  private static final String DOT_PDF = ".pdf";

  public static void saveDotFile(
      String dotString,
      String dir,
      String programName,
      String className,
      String methodName,
      String methodDesc,
      String transformation)
      throws FileNotFoundException {
    methodDesc = processMethodDesc(methodDesc);

    String dotFileName =
        dir
            + "/"
            + programName
            + "/"
            + className
            + "/"
            + methodName
            + removeSpecialChars(methodDesc)
            + "/"
            + transformation
            + "/"
            + methodName
            + removeSpecialChars(methodDesc)
            + PrettyMethodGraph.DOT_DOT;

    File file = new File(dotFileName);
    file.getParentFile().mkdirs();

    PrintWriter writer = new PrintWriter(dotFileName);
    writer.println(dotString);
    writer.flush();
    writer.close();
  }

  private static String processMethodDesc(String methodDesc) {
    if (methodDesc.length() > 100) {
      return String.valueOf(methodDesc.hashCode());
    }

    return methodDesc;
  }

  private static String removeSpecialChars(String string) {
    return string.replaceAll("/", ":");
  }

  public static void savePdfFile(
      String dir,
      String programName,
      String className,
      String methodName,
      String methodDesc,
      String transformation)
      throws IOException, InterruptedException {
    methodDesc = processMethodDesc(methodDesc);

    String fileNamePrefix =
        dir
            + "/"
            + programName
            + "/"
            + className
            + "/"
            + methodName
            + removeSpecialChars(methodDesc)
            + "/"
            + transformation
            + "/"
            + methodName
            + removeSpecialChars(methodDesc);
    String dotFileName = fileNamePrefix + PrettyMethodGraph.DOT_DOT;
    String pdfFileName = fileNamePrefix + PrettyMethodGraph.DOT_PDF;

    List<String> commandList = new ArrayList<>();
    commandList.add("dot");
    commandList.add("-Tpdf");
    commandList.add(dotFileName);
    commandList.add("-o");
    commandList.add(pdfFileName);

    String[] command = new String[commandList.size()];
    command = commandList.toArray(command);
    System.out.println(Arrays.toString(command));

    try {
      Process process = Runtime.getRuntime().exec(command);
      Executor.processOutput(process);
      Executor.processError(process);

      process.waitFor();
    } catch (RuntimeException re) {
      re.printStackTrace();
    }
  }

  @Deprecated
  public void saveDotFile(String programName, String className, String methodName) {
    throw new UnsupportedOperationException("Deprecated");
    //    this.saveDotFile(BaseRegionInstrumenter.DIRECTORY, programName, className, methodName);
  }

  @Deprecated
  public void savePdfFile(String programName, String className, String methodName) {
    //    this.savePdfFile(BaseRegionInstrumenter.DIRECTORY, programName, className, methodName);
    throw new UnsupportedOperationException("Deprecated");
  }

  public String toDotStringVerbose(String methodName) {
    Set<MethodBlock> blocks = this.getBlocks();
    Set<PrettyMethodBlock> prettyBlocks = new HashSet<>();

    for (MethodBlock methodBlock : blocks) {
      if (methodBlock.isSpecial()) {
        continue;
      }

      prettyBlocks.add((PrettyMethodBlock) methodBlock);
    }

    StringBuilder dotString = new StringBuilder("digraph " + methodName + " {\n");
    dotString.append("node [shape=record];\n");

    for (PrettyMethodBlock prettyMethodBlock : prettyBlocks) {
      dotString.append(prettyMethodBlock.getID());
      dotString.append(" [label=\"");

      List<String> prettyInstructions = prettyMethodBlock.getPrettyInstructions();

      for (String instruction : prettyInstructions) {
        instruction = instruction.replace("\"", "\\\"");
        instruction = instruction.replace("<", "\\<");
        instruction = instruction.replace(">", "\\>");
        dotString.append(instruction);
        dotString.append("\\l");
      }

      dotString.append("\"];\n");
    }

    dotString.append(this.getEntryBlock().getID());
    dotString.append(";\n");
    dotString.append(this.getExitBlock().getID());
    dotString.append(";\n");

    for (MethodBlock methodBlock : this.getBlocks()) {
      for (MethodBlock successor : methodBlock.getSuccessors()) {
        dotString.append(methodBlock.getID());
        dotString.append(" -> ");
        dotString.append(successor.getID());

        if (!methodBlock.isSpecial()
            && this.getExceptionalEdges().get(methodBlock).contains(successor)) {
          dotString.append(" [style=dashed, color=red]");
        }

        dotString.append("\n");
      }
    }

    Set<PrettyMethodBlock> instrumentedPrettyBlocks = new HashSet<>();

    for (PrettyMethodBlock prettyBlock : prettyBlocks) {
      for (String prettyInstruction : prettyBlock.getPrettyInstructions()) {
        // TODO do not hard code this
        if ((prettyInstruction.contains("Regions") || prettyInstruction.contains("RegionsCounter"))
            && (prettyInstruction.contains("enter") || prettyInstruction.contains("exit"))) {
          instrumentedPrettyBlocks.add(prettyBlock);
        }
      }
    }

    for (PrettyMethodBlock prettyBlock : instrumentedPrettyBlocks) {
      dotString.append(prettyBlock.getID());
      dotString.append("[fontcolor=\"purple\", penwidth=3, color=\"purple\"];\n");
    }

    dotString.append("}");

    return dotString.toString();
  }
}
