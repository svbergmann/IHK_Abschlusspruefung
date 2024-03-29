package com.cae.de.utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasse um alle Programmargumente analysieren zu können.
 */
public class CmdLineParser {

	private static final String INPUT_FOLDER_STRING = "-inputfolder";
	private static final String OUTPUT_FOLDER_STRING = "-outputfolder";
	private static final String LOG_STRING = "-log";
	private static final String LOG_LEVEL_STRING = "-loglvl";
	private static final Logger ROOT_LOGGER = Logger.getLogger("");
	private static final Logger LOGGER = Logger.getLogger(CmdLineParser.class.getName());
	private static final String ITERATIONEN_STRING = "-i";
	private int iterationen = 100;
	private String inputFolder;
	private String outputFolder;

	/**
	 * Konstruktor, welcher als Parameter das args-Array der main Methode übergeben bekommen soll.
	 * Hier findet dann auch schon die Analyse statt und auch das Setzen des Loggers.
	 * Die Namen der Ein- und Ausgabeordner können dann über die Getter ausgelesen werden.
	 * @param args die Programmzeilenargumente
	 */
	public CmdLineParser(String[] args) {
		this.inputFolder = "input";
		this.outputFolder = "output";
		var logOption = LogOption.TRUE;
		for (var i = 0; i < args.length; i++) {
			if (i + 1 < args.length) {
				switch (args[i]) {
					case INPUT_FOLDER_STRING -> this.inputFolder = args[++i];
					case OUTPUT_FOLDER_STRING -> this.outputFolder = args[++i];
					case LOG_STRING -> {
						try {
							logOption = LogOption.getOption(args[++i]);
						} catch (IllegalArgumentException e) {
							LOGGER.log(Level.WARNING, "Konnte " + args[i] + " keiner LOG_OPTION zuordnen."
									+ "LOG_OPTIONen sind true, false, file. Der default Wert ist false.");
						}
					}
					case ITERATIONEN_STRING -> this.iterationen = Integer.parseInt(args[++i]);
					case LOG_LEVEL_STRING -> {
						Level logLevel = switch (args[++i]) {
							case "info" -> Level.INFO;
							case "warning" -> Level.WARNING;
							default -> Level.ALL;
						};
						ROOT_LOGGER.setLevel(logLevel);
					}
				}
			}
		}

		switch (logOption) {
			case FILE -> {
				try {
					FileHandler FILE_HANDLER = new FileHandler("IHK_Abschlusspruefung.log", true);
					ROOT_LOGGER.addHandler(FILE_HANDLER);
				} catch (IOException e) {
					ROOT_LOGGER.log(Level.WARNING, "Konnte keinen FileHandler zum Logger hinzufügen. "
							+ "Logs werden in die Konsole geschrieben.");
				}
			}
			case FALSE -> {
				for (var handler : ROOT_LOGGER.getHandlers()) {
					ROOT_LOGGER.removeHandler(handler);
				}
			}
		}
	}

	public String getInputFolder() {
		return this.inputFolder;
	}

	public int getIterationen() {
		return this.iterationen;
	}

	public String getOutputFolder() {
		return this.outputFolder;
	}

	/**
	 * Enumeration für alle Logging Optionen, welche true, false oder file sind.
	 */
	public enum LogOption {
	  TRUE("true"),
	  FALSE("false"),
	  FILE("file");
	  private final String value;

	  LogOption(String value) {
	    this.value = value;
	  }

	  public static LogOption getOption(String value) {
	    for (var option : LogOption.values()) {
	      if (option.value.equals(value)) {
	        return option;
	      }
	    }
	    return FALSE;
	  }
	}
}
