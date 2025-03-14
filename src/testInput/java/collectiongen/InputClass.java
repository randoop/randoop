package collectiongen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The primary input class for generating collections
 */
public class InputClass {
  public static boolean checkDayIn(Day day, EnumSet<Day> days) {
    return days.contains(day);
  }

  public static boolean checkSeasonIn(Season season, EnumSet<Season> seasons) {
    return seasons.contains(season);
  }

  public static <E extends Enum<E>> boolean checkValueIn(E e, EnumSet<E> set) {
    return set.contains(e);
  }

  public static String dayMessage(Day day) {
    return "Oh, what a " + day + "!";
  }

  public static String seasonMessage(Season season) {
    return "Oh, what a " + season + "!";
  }

  public static <E extends Enum<E>> String enumMessage(E e) {
    return "Oh, what a " + e + "!";
  }

  public static boolean checkMemberOfAnInputClassCollection(AnInputClass aic, Collection<AnInputClass> set) {
    return set.contains(aic);
  }

  public static boolean checkMemberOfAnInputClassSet(AnInputClass aic, Set<AnInputClass> set) {
    return set.contains(aic);
  }

  public static boolean checkMemberOfAnInputClassList(AnInputClass aic, List<AnInputClass> list) {
    return list.contains(aic);
  }

  public static boolean checkMemberOfArrayList(AnInputClass aic, ArrayList<AnInputClass> list) {
    return list.contains(aic);
  }

  public static boolean checkMemberOfANonInputClassCollection(ANonInputClass aic, Collection<ANonInputClass> set) {
    return set.contains(aic);
  }

  public static boolean checkMemberOfANonInputClassSet(ANonInputClass aic, Set<ANonInputClass> set) {
    return set.contains(aic);
  }

  public static boolean checkMemberOfANonInputClassList(ANonInputClass aic, List<ANonInputClass> list) {
    return list.contains(aic);
  }

  public static boolean checkMemberOfArrayList(ANonInputClass aic, ArrayList<ANonInputClass> list) {
    return list.contains(aic);
  }

  public static <E> boolean genericCheckMemberOfParameterizedArray(List<E> l, List<E>[] array) {
    return true;
  }

  public static <E> boolean genericCheckMemberOfCollection(E e, Collection<E> set) {
    return set.contains(e);
  }

  public static <E> boolean genericCheckMemberOfSet(E aic, Set<E> set) {
    return set.contains(aic);
  }

  public static <E> boolean genericCheckMemberOfList(E aic, List<E> list) {
    return list.contains(aic);
  }

  public static <E> boolean genericCheckMemberOfArrayList(E aic, ArrayList<E> list) {
    return list.contains(aic);
  }

  public static <E> boolean genericCheckMemberOfArray(E aic, E[] array) {
    for (E e : array) {
      if (e.equals(aic)) {
        return true;
      }
    }
    return false;
  }
}
