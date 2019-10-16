package edu.cmu.cs.mvelezce.instrumenter.utils;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

public final class ASMNames {

  private ASMNames() {
    throw new UnsupportedOperationException();
  }

  public static String getPackageName(ClassNode classNode) {
    System.err.println("This logic might be wrong. Why do we care about source file?");
    String name = classNode.name;
    String sourceFile = classNode.sourceFile;
    String className = sourceFile.replace(".java", "");
    String packageName = name.replace(className, "");

    packageName = packageName.substring(0, packageName.length() - 1);

    return packageName;
  }

  public static String getClassName(ClassNode classNode) {
    System.err.println("This logic might be wrong. Why do we care about source file?");
    String sourceFile = classNode.sourceFile;

    return sourceFile.replace(".java", "");
  }

  public static String getASMPackageAndClassName(String packageName, String className) {
    return packageName + "." + className;
  }
}
