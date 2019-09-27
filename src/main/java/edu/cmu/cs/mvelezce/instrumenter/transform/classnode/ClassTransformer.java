package edu.cmu.cs.mvelezce.instrumenter.transform.classnode;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.Set;

public interface ClassTransformer {

  Set<ClassNode> readClasses() throws IOException;

  Set<ClassNode> getClassesToTransform(Set<ClassNode> classNodes);

  ClassNode readClass(String fileName) throws IOException;

  void writeClass(ClassNode classNode) throws IOException;

  String getPathToClasses();

  String getOutputDir();

  ClassWriter getClassWriter(ClassNode node);
}
