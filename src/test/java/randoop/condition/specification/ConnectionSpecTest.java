package randoop.condition.specification;

import static junit.framework.TestCase.fail;

import com.google.gson.GsonBuilder;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ConnectionSpecTest {

  @Test
  public void testSerialization() {
    Class<?> c = net.Connection.class;

    try {
      c.getConstructor();
    } catch (NoSuchMethodException e) {
      throw new Error(e);
    }

    Method mOpen;
    try {
      mOpen = c.getMethod("open");
    } catch (NoSuchMethodException e) {
      fail("didn't find method: open");
      throw new Error("dead code");
    }

    List<OperationSpecification> opList = new ArrayList<>();

    Guard throwsGuard = new Guard("if the connection is already open", "receiver.isOpen()");
    ThrowsCondition opThrows =
        new ThrowsCondition(
            "throws IllegalStateException if the connection is already open",
            throwsGuard,
            IllegalStateException.class.getName());
    List<ThrowsCondition> throwsList = Collections.singletonList(opThrows);
    OperationSignature opOpen = OperationSignature.of(mOpen);
    OperationSpecification opSpec = new OperationSpecification(opOpen, new Identifiers());
    opSpec.addThrowsConditions(throwsList);
    opList.add(opSpec);

    Method mSend;
    try {
      mSend = c.getMethod("send", int.class);
    } catch (NoSuchMethodException e) {
      fail("didn't find method: send");
      throw new Error("dead code");
    }

    Guard paramGuard = new Guard("the code must be positive", "code > 0");
    Precondition opParam = new Precondition("the code must be positive", paramGuard);
    List<Precondition> paramList = new ArrayList<>();

    paramList.add(opParam);
    List<String> paramNames = Collections.singletonList("code");
    OperationSignature opSend = OperationSignature.of(mSend);
    opSpec = new OperationSpecification(opSend, new Identifiers(paramNames));
    opSpec.addParamSpecifications(paramList);
    opList.add(opSpec);

    Method mReceive;
    try {
      mReceive = c.getMethod("receive");
    } catch (NoSuchMethodException e) {
      fail("didn't find method: receive");
      throw new Error("dead code");
    }
    Guard returnGuard = new Guard("", "true");
    Property property = new Property("received value is non-negative", "result >= 0");
    Postcondition opReturn =
        new Postcondition("returns non-negative received value", returnGuard, property);
    List<Postcondition> retList = Collections.singletonList(opReturn);
    OperationSignature opReceive = OperationSignature.of(mReceive);
    opSpec = new OperationSpecification(opReceive, new Identifiers());
    opSpec.addReturnSpecifications(retList);
    opList.add(opSpec);

    new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(opList);
  }
}
