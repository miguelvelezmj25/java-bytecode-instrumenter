package edu.cmu.cs.mvelezce.instrumenter.transform.methodnode;

import edu.cmu.cs.mvelezce.instrumenter.bytecode.MethodTracer;
import edu.cmu.cs.mvelezce.instrumenter.bytecode.TraceClassInspector;
import edu.cmu.cs.mvelezce.instrumenter.graph.PrettyMethodGraph;
import edu.cmu.cs.mvelezce.instrumenter.graph.builder.pretty.PrettyMethodGraphBuilder;
import edu.cmu.cs.mvelezce.instrumenter.transform.classnode.ClassTransformer;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.util.Printer;

import java.io.IOException;
import java.util.Set;

public abstract class BaseMethodTransformer implements MethodTransformer {

  private final String programName;
  private final ClassTransformer classTransformer;
  private final String mainClass;
  private final boolean debug;

  public BaseMethodTransformer(
      String programName, ClassTransformer classTransformer, String mainClass, boolean debug) {
    this.programName = programName;
    this.classTransformer = classTransformer;
    this.mainClass = mainClass.replaceAll("\\.", "/");
    this.debug = debug;
  }

  protected abstract String getDebugDir();

  public String getProgramName() {
    return programName;
  }

  public String getMainClass() {
    return mainClass;
  }

  // TODO override transform method to call the updateMaxs method
  @Override
  public void transformMethods() throws IOException {
    Set<ClassNode> classNodes = this.classTransformer.readClasses();
    classNodes = this.classTransformer.getClassesToTransform(classNodes);
    this.transformMethods(classNodes);
  }

  @Override
  public void transformMethods(Set<ClassNode> classNodes) throws IOException {
    for (ClassNode classNode : classNodes) {
      Set<MethodNode> methodsToInstrument = this.getMethodsToInstrument(classNode);

      if (methodsToInstrument.isEmpty()) {
        continue;
      }

      System.out.println("Transforming class " + classNode.name);

      for (MethodNode methodToInstrument : methodsToInstrument) {
        System.out.println("Transforming method " + methodToInstrument.name);
        this.transformMethod(methodToInstrument, classNode);
      }

      this.classTransformer.writeClass(classNode);

      if (debug) {
        this.debugMethods(classNode, methodsToInstrument);
      }
    }
  }

  public boolean debug() {
    return debug;
  }

  private void debugMethods(ClassNode classNode, Set<MethodNode> methodsToInstrument)
      throws IOException {
    // TODO MIGUEL delete existing files
    ClassWriter classWriter = this.classTransformer.getClassWriter(classNode);
    ClassReader classReader = new ClassReader(classWriter.toByteArray());
    classNode = new ClassNode();
    classReader.accept(classNode, 0);

    TraceClassInspector classInspector = new TraceClassInspector(classNode.name);
    MethodTracer tracer = classInspector.visitClass(classReader);

    for (MethodNode methodNode : classNode.methods) {
      if (!this.isMethodToInstrument(methodNode, methodsToInstrument)) {
        continue;
      }

      Printer printer = tracer.getPrinterForMethodSignature(methodNode.name + methodNode.desc);
      PrettyMethodGraphBuilder prettyBuilder =
          new PrettyMethodGraphBuilder(classNode, methodNode, printer);
      PrettyMethodGraph prettyGraph = prettyBuilder.build();

      String doString = prettyGraph.toDotStringVerbose(methodNode.name);

      PrettyMethodGraph.saveDotFile(
          doString,
          this.getDebugDir(),
          this.getProgramName(),
          classNode.name,
          methodNode.name,
          methodNode.desc,
          "transform");

      try {
        PrettyMethodGraph.savePdfFile(
            this.getDebugDir(),
            this.getProgramName(),
            classNode.name,
            methodNode.name,
            methodNode.desc,
            "transform");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private boolean isMethodToInstrument(MethodNode methodNode, Set<MethodNode> methodsToInstrument) {
    String name = methodNode.name;
    String desc = methodNode.desc;

    for (MethodNode methodNodeToInstrument : methodsToInstrument) {
      if (name.equals(methodNodeToInstrument.name) && desc.equals(methodNodeToInstrument.desc)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public ClassTransformer getClassTransformer() {
    return classTransformer;
  }

  //  protected void updateMaxs(MethodNode methodNode, ClassNode classNode) {
  //    MethodNode tmpMethodNode = this.getModifiedMethodNode(methodNode, classNode);
  //    methodNode.visitMaxs(tmpMethodNode.maxStack, tmpMethodNode.maxLocals);
  //  }
  //
  //  private MethodNode getModifiedMethodNode(MethodNode methodNode, ClassNode classNode) {
  //    ClassWriter classWriter = this.classTransformer.getClassWriter(classNode);
  //    ClassNode newClassNode = this.getNewClassNode(classWriter);
  //
  //    for (MethodNode method : newClassNode.methods) {
  //      if (method.name.equals(methodNode.name) && method.desc.equals(methodNode.desc)) {
  //        return method;
  //      }
  //    }
  //
  //    throw new RuntimeException("Did not find the method");
  //  }
  //
  //  private ClassNode getNewClassNode(ClassWriter classWriter) {
  //    ClassReader classReader = new ClassReader(classWriter.toByteArray());
  //    ClassNode classNode = new ClassNode();
  //    classReader.accept(classNode, 0);
  //
  //    return classNode;
  //  }

}
