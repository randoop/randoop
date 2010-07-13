package randoop.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import randoop.DummyStatement;
import randoop.StatName;
import randoop.StatsForMethod;
import randoop.util.Util;

public class StatsCollectionTest extends TestCase {

  private StatName foo;
  private StatName bar;
  private List<StatName> fooList;
  private List<StatName> fooBarList;

  @Override
  public void setUp() {
    foo = new StatName("Foo", "Foo", "Foo", true);
    bar = new StatName("Bar", "Bar", "Bar", true);
    fooList = new ArrayList<StatName>();
    fooList.add(foo);
    fooBarList = new ArrayList<StatName>();
    fooBarList.add(foo);
    fooBarList.add(bar);
  }

  public void test1() {
    StatsForMethod stats = new StatsForMethod(new DummyStatement());
    stats.addKey(foo);
    stats.addKey(bar);
    stats.addToCount(foo, 1);
    assertEquals("Foo: 1" + Util.newLine + "Bar: 0", stats.toString().trim());
    assertEquals(fooBarList, stats.getKeys());
    assertEquals(0, stats.getCount(bar));
    assertEquals(1, stats.getCount(foo));
  }

  public void test2() {
    StatsForMethod stats = new StatsForMethod(new DummyStatement(), foo);
    stats.addToCount(foo, 1);
    assertEquals("Foo: 1", stats.toString().trim());
    assertEquals(fooList, stats.getKeys());
    assertEquals(1, stats.getCount(foo));
  }
}



