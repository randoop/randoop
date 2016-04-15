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
    ConcreteTypeTuple ct1 = new ConcreteTypeTuple();
    ConcreteTypeTuple ct2 = new ConcreteTypeTuple(new ArrayList<ConcreteType>());
    assertEquals("empty tuples should be same: ", ct1, ct2);

    GenericTypeTuple gt1 = new GenericTypeTuple();
    GenericTypeTuple gt2 = new GenericTypeTuple(new ArrayList<GeneralType>());
    assertEquals("empty tuples should be the same: ", gt1, gt2);

    assertEquals("concrete empty tuples should be the same:", ct1, gt1.makeConcrete());
  }

  @Test
  public void singletonTest() {
    List<ConcreteType> pl1 = new ArrayList<>();
    pl1.add(ConcreteTypes.INT_TYPE);
    ConcreteTypeTuple ct1 = new ConcreteTypeTuple(pl1);
    ConcreteTypeTuple ct2 = new ConcreteTypeTuple(pl1);
    assertEquals("singletons should be same", ct1, ct2);
  }
}
