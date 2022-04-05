package com.cae.de.utils.algorithms;

import com.cae.de.models.Landkarte;
import com.cae.de.utils.la.Kreis;
import com.cae.de.utils.la.Punkt;
import java.util.logging.Logger;

/** Eine erste Strategie, welche wahrscheinlich noch nicht optimal ist, deshalb der Name. */
public class BruteForceStrategy implements IStrategy {

  private static final Logger LOGGER = Logger.getLogger(BruteForceStrategy.class.getName());

  @Override
  public void rechne(Landkarte landkarte) {

    for (var entry : landkarte.getBeziehungen().entrySet()) {
      var staat = entry.getKey();
      var k1 = new Kreis(staat.getX(), staat.getY(), staat.getKenngroesse());
      for (var n : entry.getValue()) {
        var k2 = new Kreis(n.getX(), n.getY(), n.getKenngroesse());
        landkarte.addKraft(staat, n, k1.getAbstand(k2));
      }
    }

    for (var entry : landkarte.getBeziehungen().entrySet()) {
      var staat = entry.getKey();
      var k1 = new Kreis(staat.getX(), staat.getY(), staat.getKenngroesse());
      for (var n : landkarte.getBeziehungen().entrySet()) {
        var staat2 = n.getKey();
        if (staat == staat2 || landkarte.getBeziehungen().get(staat).contains(staat2)) continue;
        var k2 = new Kreis(staat2.getX(), staat2.getY(), staat2.getKenngroesse());
        if (k1.getAbstand(k2) < 0) landkarte.addKraft(staat, staat2, k1.getAbstand(k2));
      }
    }

    for (var entry : landkarte.getKreafte().entrySet()) {
      var staat = entry.getKey();
      for (var n : entry.getValue().entrySet()) {
        var p1 = new Punkt(staat.getX(), staat.getY());
        var nachbarstaat = n.getKey();
        var p2 = new Punkt(nachbarstaat.getX(), nachbarstaat.getY());
        var p1new =
            (n.getValue() > 0)
                ? p1.verschiebeInRichtung(p2, n.getValue() / 2)
                : p1.verschiebeInGegenrichtung(p2, n.getValue() / 2);
        staat.setX(p1new.x());
        staat.setY(p1new.y());
      }
    }

    landkarte.removeKraefte();
  }

  public String toString() {
    return this.getClass().getSimpleName();
  }
}
