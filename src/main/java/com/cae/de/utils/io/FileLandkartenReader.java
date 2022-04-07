package com.cae.de.utils.io;

import com.cae.de.models.Landkarte;
import com.cae.de.models.Staat;
import com.cae.de.utils.algorithms.BruteForceStrategy;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasse zum Einlesen einer gegebenen Landkarte.
 */
public class FileLandkartenReader implements IReader<Landkarte> {

  private static final Logger LOGGER = Logger.getLogger(FileLandkartenReader.class.getName());

  /**
   * Liest ein Objekt des Typs {@link Landkarte} ein.
   * @param pathToFile der Pfad zur Datei
   * @return eine Landkarte
   */
  @Override
  public Landkarte readObject(String pathToFile) {
    LOGGER.log(Level.INFO, "Versuche " + pathToFile + " zu lesen.");
    try (BufferedReader br = new BufferedReader(new FileReader(pathToFile))) {
      var kenngroesse = br.readLine();
      br.readLine();
      var beziehungen = new HashMap<Staat, HashSet<Staat>>();
      while (true) {
        var line = br.readLine();
        if (line.trim().equals("# Nachbarschaften")) break;
        var splittedLine = line.split("\t");
        beziehungen.put(new Staat(splittedLine[0],
            Double.parseDouble(splittedLine[1]),
            Double.parseDouble(splittedLine[2]),
            Double.parseDouble(splittedLine[3])),
            new HashSet<>());
      }

      while (true) {
        var line = br.readLine();
        if (line == null) break;
        var splittedLine = line.split(":");
        var firstIdent = splittedLine[0];

        Arrays.stream(splittedLine[1].split("\s")).map(String::trim)
            .filter(identifier -> !identifier.equals(" ") && !identifier.equals(""))
            .map(identifier -> beziehungen
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().getIdentifier().equals(identifier))
                .findFirst()
                .get()).forEachOrdered(nachbarStaatEntry -> {
              var staatEntry = beziehungen
                  .entrySet()
                  .stream()
                  .filter(entry -> entry.getKey().getIdentifier().equals(firstIdent))
                  .findFirst()
                  .get();
              staatEntry.getValue().add(nachbarStaatEntry.getKey());
              nachbarStaatEntry.getValue().add(staatEntry.getKey());
            });
      }
      return new Landkarte(kenngroesse, beziehungen, new BruteForceStrategy());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Konnte die Datei: " + pathToFile + " nicht lesen.");
      return null;
    }
  }

  @Override
  public String read(String pathToFile) {
    return null;
  }
}
