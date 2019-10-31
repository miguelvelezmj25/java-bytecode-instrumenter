package edu.cmu.cs.mvelezce.instrumenter.graph;

import edu.cmu.cs.mvelezce.adapter.adapters.trivial.BaseTrivialAdapter;
import edu.cmu.cs.mvelezce.instrumenter.graph.block.MethodBlock;
import edu.cmu.cs.mvelezce.instrumenter.graph.builder.cfg.CFGBuilder;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class MethodGraphTest {

  @Test
  public void trivial_1()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
    String className = BaseTrivialAdapter.MAIN_CLASS;
    String classDir = BaseTrivialAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "main";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));

    System.out.println(graph.isConnectedToEntry(new MethodBlock.Builder("dumb").build()));
  }
}
