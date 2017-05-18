package randoop.test;

import java.util.ArrayList;
import java.util.List;

/**
 * class for testing Randoop handling of enums. Started as simple example and ended up basically
 * being Card example from Java language specification:
 * https://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.9
 */
public class PlayingCard {
  public enum Suit {
    CLUBS,
    DIAMONDS,
    HEARTS,
    SPADES
  }

  public enum Rank {
    DEUCE,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    JACK,
    QUEEN,
    KING,
    ACE
  }

  enum PackageEnum {
    YOU,
    SHOULD,
    NOT,
    SEE,
    ME
  }

  private enum PrivateEnum {
    YOU,
    SHOULD,
    NOT,
    SEE,
    ME
  }

  public class WhyAmIHere {
    private String justAsking = "really";

    public String getString() {
      return justAsking;
    }
  }

  private final Suit suit;
  private final Rank rank;

  public PlayingCard(Suit suit, Rank rank) {
    this.suit = suit;
    this.rank = rank;
  }

  public Suit suit() {
    return suit;
  }

  public Rank rank() {
    return rank;
  }

  public String toString() {
    return rank + " of " + suit;
  }

  public static List<PlayingCard> newDeck() {
    return new ArrayList<>(prototypeDeck);
  }

  private static final List<PlayingCard> prototypeDeck = new ArrayList<>(52);

  static {
    for (Suit suit : Suit.values())
      for (Rank rank : Rank.values()) prototypeDeck.add(new PlayingCard(suit, rank));
  }
}
