package edu.cmu.cs.mvelezce.instrumenter.transform.classnode;

import jdk.internal.org.objectweb.asm.ClassWriter;

/** This class writer seems to fix the issue when writing inner classes */
public class CustomClassWriter extends ClassWriter {

  public CustomClassWriter(int i) {
    super(i);
  }

  @Override
  protected String getCommonSuperClass(String type1, String type2) {
    ClassLoader type5 = this.getClass().getClassLoader();
    Class type3;
    Class type4;
    try {
      type3 = Class.forName(type1.replace('/', '.'), false, type5);
      type4 = Class.forName(type2.replace('/', '.'), false, type5);
    } catch (Exception type7) {
      throw new RuntimeException(type7.toString());
    }

    if (type3.isAssignableFrom(type4)) {
      return type1;
    } else if (type4.isAssignableFrom(type3)) {
      return type2;
    } else if (!type3.isInterface() && !type4.isInterface()) {
      do {
        type3 = type3.getSuperclass();
      } while (!type3.isAssignableFrom(type4));

      return type3.getName().replace('.', '/');
    } else {
      return "java/lang/Object";
    }
  }
}
