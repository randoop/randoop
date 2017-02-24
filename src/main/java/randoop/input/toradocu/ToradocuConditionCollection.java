package randoop.input.toradocu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import plume.Pair;
import plume.UtilMDE;
import randoop.condition.Condition;
import randoop.condition.ConditionCollection;
import randoop.reflection.TypeNames;
import randoop.test.ExpectedExceptionGenerator;
import randoop.test.PostConditionCheckGenerator;
import randoop.test.TestCheckGenerator;
import randoop.types.ClassOrInterfaceType;

/**
 * A {@link ConditionCollection} for Toradocu generated conditions.
 */
public class ToradocuConditionCollection implements ConditionCollection {

  /** The lists of preconditions for constructor/method */
  private final Map<AccessibleObject, List<Condition>> conditionMap;

  /** The condition-exception-type pairs for constructors/methods */
  private final Map<AccessibleObject, Map<Condition, Pair<TestCheckGenerator, TestCheckGenerator>>>
      postconditionMap;

  /**
   * Creates a {@link ToradocuConditionCollection} for the given preconditions and throws-conditions.
   *
   * @param conditionMap  the precondition map
   * @param postconditionMap  the throws-conditions map
   */
  private ToradocuConditionCollection(
      Map<AccessibleObject, List<Condition>> conditionMap,
      Map<AccessibleObject, Map<Condition, Pair<TestCheckGenerator, TestCheckGenerator>>>
          postconditionMap) {
    this.conditionMap = conditionMap;
    this.postconditionMap = postconditionMap;
  }

  /**
   * Creates a {@link ConditionCollection} from Toradocu generated JSON files and condition classes.
   *
   * @param filenames  the non-null {@code List<File>} object for the JSON file
   * @return  the collection of conditions for the methods in the JSON file
   */
  public static ToradocuConditionCollection createToradocuConditions(List<File> filenames) {
    Map<AccessibleObject, List<Condition>> conditionMap = new LinkedHashMap<>();
    Map<AccessibleObject, Map<Condition, Pair<TestCheckGenerator, TestCheckGenerator>>>
        postconditionMap = new LinkedHashMap<>();
    for (File filename : filenames) {
      List<DocumentedMethod> methodList = read(filename);
      for (int methodIndex = 0; methodIndex < methodList.size(); methodIndex++) {
        DocumentedMethod method = methodList.get(methodIndex);
        if (method.paramTags().isEmpty()
            && method.throwsTags().isEmpty()
            && method.returnTag() == null) {
          continue;
        }
        Class<?> declaringClass = getClass(method.getContainingClass());
        Class<?>[] parameterTypes = getSubjectMethodParameters(method);
        Class<?>[] parameters = parameterTypes;
        if (!method.isConstructor()) {
          parameters = createConditionMethodParameters(declaringClass, parameterTypes);
        }
        AccessibleObject subject = getCallableObject(declaringClass, method, parameterTypes);

        assert conditionMap.get(subject) == null : "do not visit a method more than once";
        Class<?> conditionClass = getConditionClass(method);

        List<Tag> paramTagList = new ArrayList<Tag>(method.paramTags());
        List<Condition> conditionList = new ArrayList<>();
        for (int tagIndex = 0; tagIndex < paramTagList.size(); tagIndex++) {
          Tag paramTag = paramTagList.get(tagIndex);
          String tagCondition = paramTag.getCondition();
          if (tagCondition != null && !tagCondition.isEmpty()) {
            String methodName = buildConditionMethodName(paramTag, tagIndex, methodIndex);
            Method conditionMethod = getConditionMethod(conditionClass, methodName, parameters);
            if (conditionMethod != null) {
              conditionList.add(new ToradocuCondition(paramTag, conditionMethod));
            }
          }
        }
        conditionMap.put(subject, conditionList);

        Map<Condition, Pair<TestCheckGenerator, TestCheckGenerator>> throwsMap =
            new LinkedHashMap<>();
        List<Tag> throwsTagList = new ArrayList<Tag>(method.throwsTags());
        for (int tagIndex = 0; tagIndex < throwsTagList.size(); tagIndex++) {
          Tag throwsTag = throwsTagList.get(tagIndex);
          String tagCondition = throwsTag.getCondition();
          if (tagCondition != null && !tagCondition.isEmpty()) {
            String methodName = buildConditionMethodName(throwsTag, tagIndex, methodIndex);
            Method conditionMethod = getConditionMethod(conditionClass, methodName, parameters);
            if (conditionMethod != null) {
              ClassOrInterfaceType exceptionType = getType(((ThrowsTag) throwsTag).exception());
              ToradocuCondition condition = new ToradocuCondition(throwsTag, conditionMethod);
              throwsMap.put(
                  condition,
                  new Pair<TestCheckGenerator, TestCheckGenerator>(
                      new ExpectedExceptionGenerator(exceptionType, condition.getComment()), null));
            }
          }
        }

        if (method.returnTag() != null) {
          ReturnTag returnTag = method.returnTag();
          String tagCondition =
              convertParameters(returnTag.getCondition(), method.getParameters().size());
          String[] conditionToks = tagCondition.split("[?:]");
          String preconditionString = conditionToks[0].trim();
          if (conditionToks.length >= 2 && !conditionToks[0].trim().isEmpty()) {
            String preMethodName = buildReturnConditionMethodName(returnTag, methodIndex, "pre");
            Method preconditionMethod =
                getConditionMethod(conditionClass, preMethodName, parameters);
            parameters = addReturnType(parameters, method);
            if (preconditionMethod != null) {
              ToradocuReturnCondition precondition =
                  new ToradocuReturnCondition(returnTag, preconditionString, preconditionMethod);
              String postTrueMethodName =
                  buildReturnConditionMethodName(returnTag, methodIndex, "truepost");
              Method postTrueConditionMethod =
                  getConditionMethod(conditionClass, postTrueMethodName, parameters);

              if (postTrueConditionMethod != null) {
                ToradocuReturnCondition truePostCondition =
                    new ToradocuReturnCondition(
                        returnTag, conditionToks[1].trim(), postTrueConditionMethod);
                if (conditionToks.length == 3) {
                  String postFalseMethodName =
                      buildReturnConditionMethodName(returnTag, methodIndex, "falsepost");
                  Method postFalseConditionMethod =
                      getConditionMethod(conditionClass, postFalseMethodName, parameters);
                  if (postFalseConditionMethod != null) {
                    ToradocuReturnCondition falsePostCondition =
                        new ToradocuReturnCondition(
                            returnTag, conditionToks[2].trim(), postFalseConditionMethod);
                    throwsMap.put(
                        precondition,
                        new Pair<TestCheckGenerator, TestCheckGenerator>(
                            new PostConditionCheckGenerator(truePostCondition),
                            new PostConditionCheckGenerator(falsePostCondition)));
                    //condition is "precondition ? truePostCondition : falsePostCondition"
                  } else {
                    //condition is "precondition ? truePostCondition : true"
                    throwsMap.put(
                        precondition,
                        new Pair<TestCheckGenerator, TestCheckGenerator>(
                            new PostConditionCheckGenerator(truePostCondition), null));
                  }
                }
              }
            }
          }
        }
        postconditionMap.put(subject, throwsMap);
      }
    }
    return new ToradocuConditionCollection(conditionMap, postconditionMap);
  }

  /**
   * Replaces the Toradocu generated parameters in a return-condition string with variable names
   * following the {@link randoop.contract.ObjectContract} convention using {@code "x0"} to
   * {@code "xn"}, for {@code n} being the number of condition method parameters.
   * Replaces {@code "target"} with {@code x0} and {@code "result"} with {@code xn}.
   *
   * @param conditionString  the {@code String} representation of the return-condition
   * @param numMethodParameters  the number of subject method parameters
   * @return the {@code conditionString} with Toradocu generated parameter names replaced by {@code "xi"}
   */
  private static String convertParameters(String conditionString, int numMethodParameters) {
    conditionString = conditionString.replace("target", "x0");
    conditionString = conditionString.replace("result", "x" + (numMethodParameters + 1));
    for (int index = 0; index < numMethodParameters; index++) {
      conditionString = conditionString.replace("args[" + index + "]", "x" + (index + 1));
    }
    return conditionString;
  }

  /**
   * Returns the list of preconditions for the given constructor/method.
   *
   * @param member  either a {@code java.lang.reflect.Method} or {@code java.lang.reflect.ConstructorCall}
   * @return the list of preconditions for the given constructor/method
   */
  public List<Condition> getPreconditions(AccessibleObject member) {
    List<Condition> conditions = conditionMap.get(member);
    if (conditions != null) {
      return conditions;
    }
    return new ArrayList<>();
  }

  /**
   * Returns the map from conditions to exceptions for the given constructor/method.
   *
   * @param member  either a {@code java.lang.reflect.Method} or {@code java.lang.reflect.ConstructorCall}
   * @return the map of throws-conditions for the given constructor/method
   */
  public Map<Condition, Pair<TestCheckGenerator, TestCheckGenerator>> getThrowsConditions(
      AccessibleObject member) {
    return postconditionMap.get(member);
  }

  /**
   * Returns the {@code AccessibleObject} reference to the {@code java.lang.reflect.Method} or
   * {@code java.lang.reflect.Constructor} for the given {@link DocumentedMethod}.
   *
   * @param declaringClass  the declaring class of the member
   * @param documentedMethod the Toradocu tagged method or constructor
   * @param parameterTypes  the types of the parameters for the method or constructor
   * @return the reflection object for the given method or constructor
   */
  private static AccessibleObject getCallableObject(
      Class<?> declaringClass, DocumentedMethod documentedMethod, Class<?>[] parameterTypes) {
    AccessibleObject subject;

    try {
      if (documentedMethod.isConstructor()) {
        subject = declaringClass.getConstructor(parameterTypes);
      } else {
        String methodName = documentedMethod.getName();
        subject = declaringClass.getMethod(methodName, parameterTypes);
      }
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(
          "Unable to find subject method for Torudocu method (" + documentedMethod + ")");
    }
    return subject;
  }

  /**
   * Get the parameters for the subject method of the {@link DocumentedMethod} in the form
   * required to retrieve the reflection method object.
   *
   * @param documentedMethod  the method with Toradocu tags
   * @return the {@code Class<?>[]} array for the method parameters
   */
  private static Class<?>[] getSubjectMethodParameters(DocumentedMethod documentedMethod) {
    Class<?>[] parameters = new Class<?>[documentedMethod.getParameters().size()];
    for (int i = 0; i < documentedMethod.getParameters().size(); i++) {
      Parameter parameter = documentedMethod.getParameters().get(i);
      parameters[i] = getClass(parameter.getType());
    }
    return parameters;
  }

  /**
   * Fixes the condition method parameters for {@link ReturnTag} condition.
   *
   * @param parameters  the {@code Class<>} array of parameter types
   * @param method  the subject method to which parameters belong
   * @return {@code parameters} extended by the {@code Class<>} for the subject method return type
   */
  private static Class<?>[] addReturnType(Class<?>[] parameters, DocumentedMethod method) {
    Class<?>[] conditionMethodParameters = new Class<?>[parameters.length + 1];
    System.arraycopy(parameters, 0, conditionMethodParameters, 0, parameters.length);
    conditionMethodParameters[parameters.length] = getClass(method.getReturnType());
    return conditionMethodParameters;
  }

  /**
   * Gets the {@code java.lang.reflect.Method} for the condition method with the given name.
   * A condition method only exists if the corresponding tag has a condition.
   *
   * @param conditionClass  the enclosing class of the condition method
   * @param methodName  the name of the condition method
   * @param parameters  the parameter types for the subject method
   * @return the reflective method object if the method has a condition
   */
  private static Method getConditionMethod(
      Class<?> conditionClass, String methodName, Class<?>[] parameters) {
    Method conditionMethod;
    try {
      conditionMethod = conditionClass.getMethod(methodName, parameters);
    } catch (NoSuchMethodException e) {
      List<String> paramTypes = new ArrayList<>();
      for (Class<?> parameter : parameters) {
        paramTypes.add(parameter.getName());
      }
      throw new IllegalArgumentException(
          "Unable to find Toradocu condition method "
              + methodName
              + "("
              + UtilMDE.join(paramTypes, ",")
              + " in class "
              + conditionClass.getName());
    }
    return conditionMethod;
  }

  /**
   * Returns the {@code Class<?>} for the given {@link randoop.input.toradocu.Type}.
   *
   * @param type  the type from the Toradocu input
   * @return the {@code Class<?>} if type represents a valid type name
   * @throws IllegalArgumentException if the type cannot be loaded
   */
  private static Class<?> getClass(randoop.input.toradocu.Type type) {
    try {
      return TypeNames.getTypeForName(type.getQualifiedName());
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Unable to load type for Toradocu input (" + type + ")");
    }
  }

  /**
   * Returns the {@link ClassOrInterfaceType} for the given {@link randoop.input.toradocu.Type}.
   *
   * @param type  the type from the Toradocu input
   * @return the {@link ClassOrInterfaceType} for type
   */
  private static ClassOrInterfaceType getType(randoop.input.toradocu.Type type) {
    return ClassOrInterfaceType.forClass(getClass(type));
  }

  /**
   * Returns the {@code Class<?>} for the condition class of the given method.
   *
   * @param method  the {@link DocumentedMethod}
   * @return the {@code Class<?>} object for the condition class of the given method
   * @throws IllegalArgumentException if the condition class name cannot be loaded
   */
  private static Class<?> getConditionClass(DocumentedMethod method) {
    Class<?> conditionClass;
    String classname = buildConditionClassName(method);
    try {
      conditionClass = Class.forName(classname);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(
          "Unable to load condition class for Toradocu input (" + classname + ")");
    }
    return conditionClass;
  }

  /**
   * Creates the array of {@code Class<?>} objects representing the type parameters of a condition
   * method.
   * By convention, this is {@code { Object, Object[] } }.
   *
   * @param declaringClass  the declaring class for the subject method
   * @param subjectParameters  the parameter types for the subject method
   * @return the type parameters for a condition method
   */
  private static Class<?>[] createConditionMethodParameters(
      Class<?> declaringClass, Class<?>[] subjectParameters) {
    @SuppressWarnings("rawtypes")
    Class<?>[] paramTypes = new Class[subjectParameters.length + 1];
    paramTypes[0] = declaringClass;
    System.arraycopy(subjectParameters, 0, paramTypes, 1, subjectParameters.length);
    return paramTypes;
  }

  /**
   * Reads the {@link DocumentedMethod} objects from the given JSON file into a list, preserving
   * the order of the objects in the JSON file.
   *
   * @param filename  the name of the JSON file
   * @return the list of {@link DocumentedMethod} objects in the JSON file, preserving the order
   */
  @SuppressWarnings("unchecked")
  private static List<DocumentedMethod> read(File filename) {
    Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    Type typeToken = (new TypeToken<List<DocumentedMethod>>() {}).getType();
    List<DocumentedMethod> methods = new ArrayList<>();
    try (BufferedReader rdr = new BufferedReader(new FileReader(filename))) {
      methods.addAll(
          (List<DocumentedMethod>) gson.fromJson(rdr, typeToken)); //cast shouldn't be needed
    } catch (IOException e) {
      e.printStackTrace();
    }
    return methods;
  }

  /**
   * Constructs the condition class name using the convention with Toradocu.
   * The class name is the qualified name of the declaring class of the methods to which the
   * belong, except with periods replaced by the underscore character.
   *
   * @param method  the {@link DocumentedMethod} with enclosing class
   * @return the name of the enclosing class of the method with {@code '.'} replaced with {@code '_'}
   */
  private static String buildConditionClassName(DocumentedMethod method) {
    String targetType = method.getTargetClass();
    return targetType + "Conditions";
  }

  /**
   * Constructs the condition method name using the convention with Toradocu.
   * Uses the index of the corresponding {@link DocumentedMethod} in the JSON list, and the
   * index of the tag in the method tag set flattened to a list.
   * The name is the letter {@code 'm'} followed by the method index, an underscore, and then the
   * tag type string followed by the tag index.
   * So, for instance, the method name for the first throws tag of the first method will be
   * {@code "m0_t0"}, while the name for the 2nd param tag of the 5th method would be {@code "m4_p1"}.
   *
   * @param tag  the tag for the condition, must be non-null
   * @param tagIndex  the position of the tag of the tag list
   * @param methodIndex  the position of the method in the JSON list
   * @return  the constructed method name
   */
  private static String buildConditionMethodName(Tag tag, int tagIndex, int methodIndex) {
    return "m" + methodIndex + "_" + tagKindString(tag) + tagIndex;
  }

  /**
   * Constructs the condition method name for a {@link ReturnTag}.
   * Similar to  {@link #buildConditionMethodName(Tag, int, int)} except this method inserts a
   * qualifier into the method name, allowing <i>pre</i>- and <i>post</i>-conditions to be identified.
   * (Also, there is only one return tag so the tag index is always 0.)
   *
   * @param tag  the return tag
   * @param methodIndex  the position of the method in the JSON list.
   * @param methodQualifier  the qualifier for the method name
   * @return the constructed method name
   */
  private static String buildReturnConditionMethodName(
      Tag tag, int methodIndex, String methodQualifier) {
    return "m" + "_" + methodQualifier + methodIndex + "_" + tagKindString(tag) + 0;
  }

  /**
   * Returns a string representing the type of the given tag.
   *
   * @param tag  the non-null tag
   * @return  the string {@code "p"} if {@code tag} is {@link Tag.Kind#PARAM}, or
   *           {@code "t"} if {@code tag} is {@link Tag.Kind#THROWS}
   * @throws IllegalStateException if {@code tag} is neither {@link Tag.Kind#PARAM} or {@link Tag.Kind#THROWS}.
   */
  private static String tagKindString(Tag tag) {
    switch (tag.getKind()) {
      case PARAM:
        return "p";
      case THROWS:
        return "t";
      case RETURN:
        return "r";
      default:
        throw new IllegalStateException("Tag class " + tag.getClass() + " not supported.");
    }
  }
}
