package edu.cmu.cs.mvelezce.instrumenter.instrument;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public interface Instrumenter {

  void instrument(String[] args)
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException,
          InterruptedException;

  void instrument()
      throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException;

  void compile() throws IOException, InterruptedException;
}
