package edu.cmu.cs.mvelezce.instrumenter.transform.methodnode;

import edu.cmu.cs.mvelezce.adapter.adapters.trivial.BaseTrivialAdapter;
import edu.cmu.cs.mvelezce.instrumenter.transform.classnode.DefaultClassTransformer;
import edu.cmu.cs.mvelezce.utils.config.Options;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

public class BaseMethodTransformerTest {

  @Test
  public void trivial()
      throws InvocationTargetException, NoSuchMethodException, IOException, IllegalAccessException {
    String programName = BaseTrivialAdapter.PROGRAM_NAME;
    String classDir = BaseTrivialAdapter.INSTRUMENTED_CLASS_PATH;
    String mainClass = BaseTrivialAdapter.MAIN_CLASS;

    BaseMethodTransformer methodTransformer =
        new InstructionPrinterMethodTransformer(programName, classDir, mainClass);
    methodTransformer.transformMethods();
  }

  private static class InstructionPrinterMethodTransformer extends BaseMethodTransformer {

    InstructionPrinterMethodTransformer(String programName, String classDir, String mainClass)
        throws NoSuchMethodException, MalformedURLException, IllegalAccessException,
            InvocationTargetException {
      super(programName, new DefaultClassTransformer(classDir), mainClass, true);
    }

    @Override
    protected String getDebugDir() {
      return Options.DIRECTORY;
    }

    @Override
    public Set<MethodNode> getMethodsToInstrument(ClassNode classNode) {
      return new HashSet<>(classNode.methods);
    }

    @Override
    public void transformMethod(MethodNode methodNode, ClassNode classNode) {}
  }
}
