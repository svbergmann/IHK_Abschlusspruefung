package com.cae.de.problem;

import com.cae.de.framework.Observable;
import com.cae.de.framework.Observer;
import com.cae.de.framework.ProcessRunnable;
import com.cae.de.framework.ReadRunnable;
import com.cae.de.utils.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementierung eines {@link ProcessRunnable}s, {@link Observer}s und {@link Observable}s. Dieser
 * Thread kümmert sich um die Verarbeitung der Daten, welche von einem {@link ReadRunnable} und
 * {@link Observable} eingelesen und weitergegeben wurden. Intern wird das Master-Worker Pattern
 * implementiert.
 */
public class ThreadB extends Observable<AutoKorrelationsFunktion>
    implements Observer<Pair<Data, Integer>>, ProcessRunnable<Data, AutoKorrelationsFunktion> {

  private static final Logger LOGGER = Logger.getLogger(ThreadB.class.getName());
  private final Set<String> processedFiles = new HashSet<>();
  private boolean running = false;

  /**
   * Methode für die Bearbeitung eines {@link Data} Objektes. Hier werden {@link CompletableFuture}s
   * genutzt, um die Arbeit auf verschiedene, freie Threads aufteilen zu können. Jedes Mal wenn ein
   * Thread fertig ist, werden alle {@link Observer} über die neue {@link AutoKorrelationsFunktion}
   * informiert.
   *
   * @param data das Objekt der Eingabe
   * @return eine {@link AutoKorrelationsFunktion}
   */
  @Override
  public AutoKorrelationsFunktion process(Data data) {
    if (this.processedFiles.contains(data.fileName())) return null;
    this.processedFiles.add(data.fileName());
    var cf =
        CompletableFuture.supplyAsync(
                () -> {
                  LOGGER.log(
                      Level.INFO,
                      "ThreadB schickt \""
                          + data.fileName()
                          + "\" zur Verarbeitung an "
                          + Thread.currentThread().getName()
                          + "!");
                  return Algorithms.solve(data);
                })
            .thenAccept(this::notifyObserver);
    return null;
  }

  /** Methode, die dafür da ist, den Zustand dieses Threads zu setzten. */
  @Override
  public void run() {
    if (this.running) return;
    this.running = true;
  }

  /**
   * Update-Methode, welche testet, ob das neue Objekt schon verarbeitet wurde. Wenn dies der Fall
   * ist, wird einfach nichts getan, ansonsten wird {@link this#process(Data)} aufgerufen und der
   * Dateiname der Variable {@link this#processedFiles} hinzugefügt. Falls genauso viele Namen von
   * Dateien darin stehen, wie im Wert des Übergabeobjektes gegeben, wird auf alle Worker gewartet,
   * bis sie ihre Arbeit beendet haben, dann ein Log geschrieben und dann das Programm beendet.
   *
   * @param dataIntegerPair das Paar aus einem {@link Data} Objekt und der Größe des Ordners, bzw.
   *     der Anzahl der Daten darin
   */
  @Override
  public void update(Pair<Data, Integer> dataIntegerPair) {
    if (this.processedFiles.size() == dataIntegerPair.value()) {
      this.notifyObserver(null);
      LOGGER.log(
          Level.INFO,
          "Alle Dateien des Eingabeordners verarbeitet. " +
              "Programm wird beendet, sobald alle Daten geschrieben sind.");
    }
    if (!this.processedFiles.contains(dataIntegerPair.key().fileName())) {
      this.process(dataIntegerPair.key());
      this.processedFiles.add(dataIntegerPair.key().fileName());
    }
  }
}
