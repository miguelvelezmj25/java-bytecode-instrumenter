package edu.cmu.cs.mvelezce.instrumenter.bytecode;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.util.TraceClassVisitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class TraceClassInspector {

  private String className;

  public TraceClassInspector(String className) {
    this.className = className;
  }

  @Deprecated
  public MethodTracer visitClass() throws IOException {
    MethodTracer methodTracer = this.getMethodTracer();
    ClassReader classReader = new ClassReader(this.className);
    classReader.accept(methodTracer, 0);

    return methodTracer;
  }

  public MethodTracer visitClass(ClassReader classReader) {
    MethodTracer methodTracer = this.getMethodTracer();
    classReader.accept(methodTracer, 0);

    return methodTracer;
  }

  private MethodTracer getMethodTracer() {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    TraceClassVisitor traceClassVisitor = new TraceClassVisitor(printWriter);
    return new MethodTracer(Opcodes.ASM5, traceClassVisitor);
  }
}
