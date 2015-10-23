package randoop.test;

import java.util.List;
import java.util.TreeMap;

/**
 * CoinPurse class using {@link Coin} enum for use in Randoop tests.
 * 
 * @author bjkeller
 *
 */
public class CoinPurse {
  private TreeMap<Coin, Integer> purse;

  public CoinPurse() {
    purse = new TreeMap<Coin,Integer>();
  }
  
  public int value() {
    int val = 0;
    for (Coin c : purse.keySet()) {
      val += c.value()*purse.getOrDefault(c, 0);
    }
    return val;
  }
  
  public void add(List<Coin> coins) {
    for (Coin c: coins) {
      add(c);
    }
  }
  
  public void add(Coin c) {
    int count = purse.getOrDefault(c, 0) +1;
    purse.put(c, count);
  }
}
