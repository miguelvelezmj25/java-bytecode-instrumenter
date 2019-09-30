package edu.cmu.cs.mvelezce.instrumenter.instrument;

import edu.cmu.cs.mvelezce.utils.Options;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public abstract class BaseInstrumenter implements Instrumenter {
  private String programName;
  private String srcDir;
  private String classDir;

  public BaseInstrumenter(String programName, String srcDir, String classDir) {
    this.programName = programName;
    this.srcDir = srcDir;
    this.classDir = classDir;
  }

  @Override
  public void instrument(String[] args)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException,
          InterruptedException {
    Options.getCommandLine(args);

    if (Options.checkIfDeleteResult()) {
      this.compile();
    }

    if (Options.checkIfSave()) {
      this.instrument();
    }
  }

  public String getProgramName() {
    return programName;
  }

  public String getSrcDir() {
    return srcDir;
  }

  public String getClassDir() {
    return classDir;
  }
}
