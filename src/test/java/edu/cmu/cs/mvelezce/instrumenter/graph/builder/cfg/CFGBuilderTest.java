package edu.cmu.cs.mvelezce.instrumenter.graph.builder.cfg;

import edu.cmu.cs.mvelezce.adapter.adapters.pngtastic.BasePngtasticAdapter;
import edu.cmu.cs.mvelezce.adapter.adapters.trivial.BaseTrivialAdapter;
import edu.cmu.cs.mvelezce.instrumenter.graph.MethodGraph;
import edu.cmu.cs.mvelezce.instrumenter.transform.classnode.DefaultClassTransformer;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class CFGBuilderTest {

  @Test
  public void trivial_1()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = BaseTrivialAdapter.MAIN_CLASS;
    String classDir = BaseTrivialAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = this.getClassNode(className, classDir);

    String methodName = "main";
    MethodNode methodNode = this.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  @Test
  public void counter_1()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = "counter.com.googlecode.pngtastic.core.PngColorCounter";
    String classDir = BasePngtasticAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = this.getClassNode(className, classDir);

    String methodName = "getColors";
    MethodNode methodNode = this.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  private MethodNode getMethodNode(String methodName, ClassNode classNode) {
    for (MethodNode methodNode : classNode.methods) {
      if (methodNode.name.equals(methodName)) {
        return methodNode;
      }
    }

    throw new RuntimeException("Could not find method " + methodName);
  }

  private ClassNode getClassNode(String mainClass, String classDir)
      throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    DefaultClassTransformer classTransformer = new DefaultClassTransformer(classDir);
    Set<ClassNode> classes = classTransformer.readClasses();

    mainClass = mainClass.replaceAll("\\.", "/");

    for (ClassNode classNode : classes) {
      if (classNode.name.equals(mainClass)) {
        return classNode;
      }
    }

    throw new RuntimeException("Could not find main class " + mainClass);
  }
}
