package randoop.test;

import java.util.List;
import java.util.TreeMap;

/** CoinPurse class using {@link Coin} enum for use in Randoop tests. */
public class CoinPurse {
  private TreeMap<Coin, Integer> purse;

  public CoinPurse() {
    purse = new TreeMap<>();
  }

  public int value() {
    int val = 0;
    for (Coin c : purse.keySet()) {
      int count = 0;
      if (purse.containsKey(c)) {
        count = purse.get(c);
      }
      val += c.value() * count;
    }
    return val;
  }

  public void add(List<Coin> coins) {
    for (Coin c : coins) {
      add(c);
    }
  }

  public void add(Coin c) {
    int count = 0;
    if (purse.containsKey(c)) {
      count = purse.get(c);
    }
    purse.put(c, count + 1);
  }
}
