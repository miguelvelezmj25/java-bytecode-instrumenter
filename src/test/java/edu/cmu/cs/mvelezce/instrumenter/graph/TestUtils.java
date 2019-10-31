package edu.cmu.cs.mvelezce.instrumenter.graph;

import edu.cmu.cs.mvelezce.instrumenter.transform.classnode.DefaultClassTransformer;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public final class TestUtils {

  private TestUtils() {}

  public static MethodNode getMethodNode(String methodName, ClassNode classNode) {
    for (MethodNode methodNode : classNode.methods) {
      if (methodNode.name.equals(methodName)) {
        return methodNode;
      }
    }

    throw new RuntimeException("Could not find method " + methodName);
  }

  public static ClassNode getClassNode(String mainClass, String classDir)
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
