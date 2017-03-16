package randoop.condition.specification;

import static junit.framework.TestCase.fail;

import com.google.gson.GsonBuilder;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/** Created by bjkeller on 3/14/17. */
public class ConnectionSpecTest {

  @Test
  public void testSerialization() {
    Class<?> c = net.Connection.class;

    Constructor<?> constructor = null;
    try {
      constructor = c.getConstructor();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }

    Method m = null;
    try {
      m = c.getMethod("open");
    } catch (NoSuchMethodException e) {
      fail("didn't find method: open");
    }
    assert m != null;

    List<OperationSpecification> opList = new ArrayList<>();

    Guard throwsGuard = new Guard("if the connection is already open", "receiver.isOpen()");
    ThrowsSpecification opThrows =
        new ThrowsSpecification(
            "throws IllegalStateException if the connection is already open",
            throwsGuard,
            IllegalStateException.class.getCanonicalName());
    List<ThrowsSpecification> throwsList = new ArrayList<>();
    throwsList.add(opThrows);
    Operation op = Operation.getOperation(m);
    OperationSpecification opSpec = new OperationSpecification(op, new Identifiers());
    opSpec.addThrowsSpecifications(throwsList);
    opList.add(opSpec);

    m = null;
    try {
      m = c.getMethod("send", int.class);
    } catch (NoSuchMethodException e) {
      fail("didn't find method: send");
    }
    assert m != null;

    Guard paramGuard = new Guard("the code must be positive", "code > 0");
    ParamSpecification opParam = new ParamSpecification("the code must be positive", paramGuard);
    List<ParamSpecification> paramList = new ArrayList<>();
    throwsGuard = new Guard("the connection is not open", "!receiver.isOpen()");
    opThrows =
        new ThrowsSpecification(
            "throws IllegalStateException if the connection is not open",
            throwsGuard,
            IllegalStateException.class.getCanonicalName());

    paramList.add(opParam);
    List<String> paramNames = new ArrayList<>();
    paramNames.add("code");
    op = Operation.getOperation(m);
    opSpec = new OperationSpecification(op, new Identifiers(paramNames));
    opSpec.addParamSpecifications(paramList);
    opList.add(opSpec);

    m = null;
    try {
      m = c.getMethod("receive");
    } catch (NoSuchMethodException e) {
      fail("didn't find method: receive");
    }
    Guard returnGuard = new Guard("", "true");
    Property property = new Property("received value is non-negative", "result >= 0");
    ReturnSpecification opReturn =
        new ReturnSpecification("returns non-negative received value", returnGuard, property);
    List<ReturnSpecification> retList = new ArrayList<>();
    retList.add(opReturn);
    op = Operation.getOperation(m);
    opSpec = new OperationSpecification(op);
    opSpec.addReturnSpecifications(retList);
    opList.add(opSpec);

    System.out.println(
        new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(opList));
  }
}
