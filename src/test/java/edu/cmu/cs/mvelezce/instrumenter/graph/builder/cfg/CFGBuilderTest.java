package edu.cmu.cs.mvelezce.instrumenter.graph.builder.cfg;

import edu.cmu.cs.mvelezce.adapter.adapters.indexFiles.BaseIndexFilesAdapter;
import edu.cmu.cs.mvelezce.adapter.adapters.measureDiskOrderedScan.BaseMeasureDiskOrderedScanAdapter;
import edu.cmu.cs.mvelezce.adapter.adapters.pngtastic.BasePngtasticAdapter;
import edu.cmu.cs.mvelezce.adapter.adapters.trivial.BaseTrivialAdapter;
import edu.cmu.cs.mvelezce.adapter.adapters.tryCatchFinally.BaseTryCatchFinallyAdapter;
import edu.cmu.cs.mvelezce.adapter.adapters.tryReturnCatch.BaseTryReturnCatchAdapter;
import edu.cmu.cs.mvelezce.adapter.adapters.whileTrueNoReturn.BaseWhileTrueNoReturnAdapter;
import edu.cmu.cs.mvelezce.instrumenter.graph.MethodGraph;
import edu.cmu.cs.mvelezce.instrumenter.graph.TestUtils;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class CFGBuilderTest {

  @Test
  public void trivial_1()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = BaseTrivialAdapter.MAIN_CLASS;
    String classDir = BaseTrivialAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "main";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  @Test
  public void trivial_2()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = BaseTryReturnCatchAdapter.MAIN_CLASS;
    String classDir = BaseTryReturnCatchAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "main";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  @Test
  public void trivial_3()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = BaseTryCatchFinallyAdapter.MAIN_CLASS;
    String classDir = BaseTryCatchFinallyAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "main";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  @Test
  public void berkeley_1()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = "com.sleepycat.je.dbi.MemoryBudget";
    String classDir = BaseMeasureDiskOrderedScanAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "<clinit>";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  @Test
  public void berkeley_2()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = "com.sleepycat.je.tree.LN";
    String classDir = BaseMeasureDiskOrderedScanAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "logInternal";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  @Test
  public void berkeley_3()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = "com.sleepycat.je.rep.impl.networkRestore.FeederManager";
    String classDir = BaseMeasureDiskOrderedScanAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "run";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  @Test
  public void berkeley_4()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = "com.sleepycat.je.util.DbRunAction$StatsPrinter";
    String classDir = BaseMeasureDiskOrderedScanAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "run";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  @Test
  public void berkeley_5()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = "com.sleepycat.je.rep.impl.node.ReplicaFactory$1";
    String classDir = BaseMeasureDiskOrderedScanAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "doRunReplicaLoopInternalWork";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  @Test
  public void berkeley_6()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = "com.sleepycat.je.log.FileManager$LogEndFileDescriptor";
    String classDir = BaseMeasureDiskOrderedScanAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "enqueueWrite";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  @Test
  public void berkeley_7()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = "com.sleepycat.je.rep.arbiter.impl.ArbiterVLSNTracker";
    String classDir = BaseMeasureDiskOrderedScanAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "readNodeId";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  @Test
  public void whileTrueNoReturn_1()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = BaseWhileTrueNoReturnAdapter.MAIN_CLASS;
    String classDir = BaseWhileTrueNoReturnAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "some";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  @Test
  public void counter_1()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = "counter.com.googlecode.pngtastic.core.PngColorCounter";
    String classDir = BasePngtasticAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "getColors";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }

  @Test
  public void lucene_1()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
    String className = "org.apache.lucene.core.search.ConjunctionDISI$BitSetConjunctionDISI";
    String classDir = BaseIndexFilesAdapter.INSTRUMENTED_CLASS_PATH;
    ClassNode classNode = TestUtils.getClassNode(className, classDir);

    String methodName = "doNext";
    MethodNode methodNode = TestUtils.getMethodNode(methodName, classNode);

    MethodGraph graph = CFGBuilder.getCfg(methodNode, classNode);
    System.out.println(graph.toDotString(methodNode.name));
  }
}
