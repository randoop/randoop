package randoop.condition.specification;

import static junit.framework.TestCase.fail;

import com.google.gson.GsonBuilder;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/** Created by bjkeller on 3/14/17. */
public class ConnectionSpecTest {

  @Test
  public void testSerialization() {
    Class<?> c = net.Connection.class;
    Method m = null;
    try {
      m = c.getMethod("open");
    } catch (NoSuchMethodException e) {
      fail("didn't find method: open");
    }
    assert m != null;

    List<OperationSpecification> opList = new ArrayList<>();

    Guard guard = new Guard("if the connection is already open", "receiver.isOpen()");
    ThrowsSpecification opThrows =
        new ThrowsSpecification(
            "throws IllegalStateException if the connection is already open",
            guard,
            IllegalStateException.class.getCanonicalName());
    List<ThrowsSpecification> throwsList = new ArrayList<>();
    throwsList.add(opThrows);
    Operation op = Operation.getOperation(m, new ArrayList<String>());
    OperationSpecification opSpec = new OperationSpecification(op);
    opSpec.addThrowsSpecifications(throwsList);
    opList.add(opSpec);

    m = null;
    try {
      m = c.getMethod("send", int.class);
    } catch (NoSuchMethodException e) {
      fail("didn't find method: send");
    }
    assert m != null;

    guard = new Guard("must be positive", "code > 0");
    ParamSpecification opParam = new ParamSpecification("the code must be positive", guard);
    List<ParamSpecification> paramList = new ArrayList<>();
    paramList.add(opParam);
    List<String> paramNames = new ArrayList<>();
    paramNames.add("code");
    op = Operation.getOperation(m, paramNames);
    opSpec = new OperationSpecification(op);
    opSpec.addParamSpecifications(paramList);
    opList.add(opSpec);

    System.out.println(
        new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(opList));
  }
}
