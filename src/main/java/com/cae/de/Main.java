package com.cae.de;

import com.cae.de.models.Landkarte;
import com.cae.de.utils.LogOption;
import com.cae.de.utils.io.FileLandkartenReader;
import com.cae.de.utils.io.GnuPlotWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class which is to be started and executed. It evaluates the given arguments and starts the
 * program if everything went right.
 */
public class Main {

  private static final String INPUT_FOLDER_STRING = "-inputfolder";
  private static final String OUTPUT_FOLDER_STRING = "-outputfolder";
  private static final String LOG_STRING = "-log";
  private static final Logger ROOT_LOGGER = Logger.getLogger("");
  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
  private static final String ITERATIONEN = "-i";
  public static FileHandler FILE_HANDLER;
  private static int iterationen = 100;

  public static void main(String[] args) {
    var inputFolder = "input";
    var outputFolder = "output";
    var logOption = LogOption.TRUE;
    for (var i = 0; i < args.length; i++) {
      if (i + 1 < args.length) {
        switch (args[i]) {
          case INPUT_FOLDER_STRING -> inputFolder = args[++i];
          case OUTPUT_FOLDER_STRING -> outputFolder = args[++i];
          case LOG_STRING -> {
            try {
              logOption = LogOption.getOption(args[++i]);
            } catch (IllegalArgumentException e) {
              LOGGER.log(Level.WARNING, "Could not cast \"" + args[i] + "\" to any LOG_OPTION."
                  + "LOG_OPTIONS are \"true\", \"false\", \"file\". Default value is \"false\".");
            }
          }
          case ITERATIONEN -> iterationen = Integer.parseInt(args[++i]);
        }
      }
    }

    // Default case for java logger is true so there is no case for it.
    switch (logOption) {
      case FILE -> {
        try {
          FILE_HANDLER = new FileHandler("IHK_Abschlusspruefung.log", true);
          ROOT_LOGGER.addHandler(FILE_HANDLER);
        } catch (IOException e) {
          ROOT_LOGGER.log(Level.WARNING, "Could not attach FileHandler to Logger. "
              + "Logs are written to the console now.");
        }
      }
      case FALSE -> {
        for (var handler : ROOT_LOGGER.getHandlers()) {
          ROOT_LOGGER.removeHandler(handler);
        }
      }
    }

    var reader = new FileLandkartenReader();
    var landkarten = new HashSet<Landkarte>();
    try {
      landkarten.addAll(Files.list(Path.of(inputFolder))
          .parallel()
          .map(f -> reader.readObject(f.toString()))
          .toList());
    } catch (IOException  e) {
      LOGGER.log(Level.SEVERE, "Could not read input files inside " + inputFolder);
      System.exit(1);
    }

    landkarten.stream()
        .parallel()
        .forEach(landkarte -> landkarte.setIterationen(iterationen));

    landkarten.stream()
        .parallel()
        .forEach(landkarte -> landkarte.rechne(iterationen));

    var writer = new GnuPlotWriter();
    try {
      if (!Files.exists(Path.of(outputFolder))) {
        Files.createDirectory(Path.of(outputFolder));
      }
      for (var i = 0; i < landkarten.size(); i++) {
        var outputPath = outputFolder + "/" + "landkarte" + "_" + i + "_out.txt";
        if (Files.exists(Path.of(outputPath))) {
          LOGGER.log(Level.WARNING, "File " + outputPath + " exists. Going to override it.");
        }
        landkarten.forEach(landkarte -> writer.write(landkarte, outputPath));
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not write files to output folder: " + outputFolder);
      System.exit(1);
    }
  }
}
