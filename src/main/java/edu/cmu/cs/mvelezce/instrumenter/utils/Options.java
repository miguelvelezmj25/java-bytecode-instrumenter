package edu.cmu.cs.mvelezce.instrumenter.utils;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public final class Options {
  public static final String DIRECTORY = "src/main/resources";
  private static final String DELRES = "delres";
  private static final String SAVERES = "saveres";
  private static final String ITERATIONS = "i";
  private static CommandLine cmd = null;

  static {
    System.err.println("Check the design of this Options class");
  }

  private Options() {}

  public static void getCommandLine(String[] args) {
    org.apache.commons.cli.Options componentOptions = new org.apache.commons.cli.Options();

    Option componentOption = new Option(Options.DELRES, "Deletes the stored result");
    componentOptions.addOption(componentOption);

    componentOption = new Option(Options.SAVERES, "Saves the result");
    componentOptions.addOption(componentOption);

    componentOption = new Option(Options.ITERATIONS, true, "Iterations");
    componentOptions.addOption(componentOption);

    CommandLineParser parser = new DefaultParser();

    try {
      Options.cmd = parser.parse(componentOptions, args);
    } catch (ParseException e) {
      throw new RuntimeException("Could not parse the options you provided");
    }
  }

  public static void checkIfDeleteResult(File file) throws IOException {
    if (cmd.hasOption(Options.DELRES)) {
      if (file.exists()) {
        FileUtils.forceDelete(file);
      }
    }
  }

  public static boolean checkIfSave() {
    return cmd.hasOption(Options.SAVERES);
  }

  public static boolean checkIfDeleteResult() {
    return cmd.hasOption(Options.DELRES);
  }

  public static int getIterations() {
    String iterations = cmd.getOptionValue(Options.ITERATIONS).trim();
    return Integer.parseInt(iterations);
  }
}
