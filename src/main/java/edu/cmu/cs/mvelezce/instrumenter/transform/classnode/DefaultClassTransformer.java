package edu.cmu.cs.mvelezce.instrumenter.transform.classnode;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

public class DefaultClassTransformer extends BaseClassTransformer {

  public DefaultClassTransformer(String pathToClasses)
      throws InvocationTargetException, NoSuchMethodException, MalformedURLException,
          IllegalAccessException {
    super(pathToClasses);
  }
}
