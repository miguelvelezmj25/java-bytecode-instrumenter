package edu.cmu.cs.mvelezce.instrumenter.transform.classnode;

import edu.cmu.cs.mvelezce.instrumenter.utils.ASMNames;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import org.apache.commons.io.FileUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseClassTransformer implements ClassTransformer {

  private final String pathToClasses;
  private final String outputDir;
  private final Set<String> classesToTransform = new HashSet<>();

  public BaseClassTransformer(String pathToClasses)
      throws NoSuchMethodException, MalformedURLException, IllegalAccessException,
          InvocationTargetException {
    this(pathToClasses, pathToClasses.replace("original", "instrumented"));
  }

  public BaseClassTransformer(String pathToClasses, String outputDir)
      throws NoSuchMethodException, MalformedURLException, IllegalAccessException,
          InvocationTargetException {
    this.pathToClasses = pathToClasses;
    this.outputDir = outputDir;

    this.addToClassPath(this.pathToClasses);
    // TODO do not delete the output directory since we might pass that directory as the source of
    // the files
    //    this.removeOutputDir();
  }

  public BaseClassTransformer(String pathToClasses, Set<String> classesToTransform)
      throws InvocationTargetException, NoSuchMethodException, MalformedURLException,
          IllegalAccessException {
    this(pathToClasses);
    this.classesToTransform.addAll(classesToTransform);
  }

  private void addToClassPath(String pathToClass)
      throws NoSuchMethodException, MalformedURLException, InvocationTargetException,
          IllegalAccessException {
    File file = new File(pathToClass);
    URI uri = file.toURI();

    URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class<URLClassLoader> urlClass = URLClassLoader.class;
    Method method = urlClass.getDeclaredMethod("addURL", URL.class);
    method.setAccessible(true);
    method.invoke(urlClassLoader, uri.toURL());
  }

  @Override
  public Set<ClassNode> getClassesToTransform(Set<ClassNode> classNodes) {
    if (this.classesToTransform.isEmpty()) {
      return classNodes;
    }

    Set<ClassNode> classesToTransform = new HashSet<>();

    for (ClassNode classNode : classNodes) {
      String packageName = ASMNames.getPackageName(classNode);
      String className = ASMNames.getClassName(classNode);

      if (this.classesToTransform.contains(
          ASMNames.getASMPackageAndClassName(packageName, className))) {
        classesToTransform.add(classNode);
      }
    }

    if (classesToTransform.isEmpty()) {
      throw new RuntimeException(
          "Could not find the class nodes for the classes to instrument: "
              + this.classesToTransform);
    }

    return classesToTransform;
  }

  @Override
  public Set<ClassNode> readClasses() throws IOException {
    Set<ClassNode> classNodes = new HashSet<>();
    String[] extensions = {"class"};

    Collection<File> files = FileUtils.listFiles(new File(this.pathToClasses), extensions, true);

    for (File file : files) {
      String fullPath = file.getPath();
      String path = fullPath.replace(".class", "");
      String relativePath = path.replace(this.pathToClasses, "");
      String qualifiedName = relativePath.replace("/", ".");

      if (qualifiedName.startsWith(".")) {
        qualifiedName = qualifiedName.substring(1);
      }

      classNodes.add(this.readClass(qualifiedName));
    }

    return classNodes;
  }

  @Override
  public ClassNode readClass(String fileName) throws IOException {
    ClassReader classReader = new ClassReader(fileName);
    ClassNode classNode = new ClassNode();
    classReader.accept(classNode, 0);

    return classNode;
  }

  @Override
  public void writeClass(ClassNode classNode) throws IOException {
    ClassWriter classWriter = this.getClassWriter(classNode);
    this.makeOutputDir(classNode);
    File outputFile = new File(outputDir + "/" + classNode.name + ".class");

    DataOutputStream output = new DataOutputStream(new FileOutputStream(outputFile));
    output.write(classWriter.toByteArray());
    output.flush();
    output.close();
  }

  @Override
  public ClassWriter getClassWriter(ClassNode classNode) {
    ClassWriter classWriter = new CustomClassWriter(ClassWriter.COMPUTE_FRAMES);
    //        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS |
    // ClassWriter.COMPUTE_FRAMES);
    //    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    //        ClassWriter classWriter = new ClassWriter(0);
    classNode.accept(classWriter);

    return classWriter;
  }

  private void makeOutputDir(ClassNode classNode) {
    String packageName = ASMNames.getPackageName(classNode);
    File outputDirFile = new File(outputDir + "/" + packageName);
    outputDirFile.mkdirs();
  }

  private void removeOutputDir() {
    File outputDirFile = new File(this.outputDir);
    this.deleteFolder(outputDirFile);
  }

  private void deleteFolder(File folder) {
    File[] files = folder.listFiles();
    if (files != null) { // some JVMs return null for empty dirs
      for (File file : files) {
        if (file.isDirectory()) {
          deleteFolder(file);
        } else {
          file.delete();
        }
      }
    }

    folder.delete();
  }

  @Override
  public String getPathToClasses() {
    return this.pathToClasses;
  }

  @Override
  public String getOutputDir() {
    return outputDir;
  }

  //  protected ClassNode getModifiedClassNode(ClassNode classNode) {
  //    ClassWriter classWriter = this.getClassWriter(classNode);
  //    ClassReader classReader = new ClassReader(classWriter.toByteArray());
  //    ClassNode newClassNode = new ClassNode();
  //    classReader.accept(newClassNode, 0);
  //
  //    return newClassNode;
  //  }

}
