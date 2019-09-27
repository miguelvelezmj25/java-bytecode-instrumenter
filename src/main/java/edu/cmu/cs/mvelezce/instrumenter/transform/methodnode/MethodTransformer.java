package edu.cmu.cs.mvelezce.instrumenter.transform.methodnode;

import edu.cmu.cs.mvelezce.instrumenter.transform.classnode.ClassTransformer;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.Set;

public interface MethodTransformer {

  Set<MethodNode> getMethodsToInstrument(ClassNode classNode);

  void transformMethod(MethodNode methodNode, ClassNode classNode);

  void transformMethods() throws IOException;

  void transformMethods(Set<ClassNode> classNodes) throws IOException;

  ClassTransformer getClassTransformer();
}
