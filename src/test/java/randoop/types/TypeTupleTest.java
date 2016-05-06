package randoop.types;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by bjkeller on 4/4/16.
 */
public class TypeTupleTest {

  @Test
  public void emptyTupleTest() {
    TypeTuple ct1 = new TypeTuple();
    TypeTuple ct2 = new TypeTuple(new ArrayList<GeneralType>());
    assertEquals("empty tuples should be same: ", ct1, ct2);

    TypeTuple gt1 = new TypeTuple();
    TypeTuple gt2 = new TypeTuple(new ArrayList<GeneralType>());
    assertEquals("empty tuples should be the same: ", gt1, gt2);

    assertEquals("concrete empty tuples should be the same:", ct1, gt1);
  }

  @Test
  public void singletonTest() {
    List<GeneralType> pl1 = new ArrayList<>();
    pl1.add(ConcreteTypes.INT_TYPE);
    TypeTuple ct1 = new TypeTuple(pl1);
    TypeTuple ct2 = new TypeTuple(pl1);
    assertEquals("singletons should be same", ct1, ct2);
  }
}
